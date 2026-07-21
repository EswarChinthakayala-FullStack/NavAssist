#!/usr/bin/env python
import os
import sys
import subprocess
import argparse
import logging
import platform
import time
import signal

# Configure logging style for a professional CLI tool
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("NavAssist-Bootstrapper")

child_processes = []


def signal_handler(sig, frame):
    """
    Cleans up all spawned background worker processes on Ctrl+C or SIGTERM.
    """
    logger.info("Termination signal received. Cleaning up processes...")
    for proc in child_processes:
        if proc.poll() is None:
            logger.info(f"Terminating subprocess PID {proc.pid}...")
            # Send termination signal
            proc.terminate()
            try:
                proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                logger.warning(f"Subprocess PID {proc.pid} timed out. Forcing kill...")
                proc.kill()
    logger.info("Clean shutdown complete.")
    sys.exit(0)


# Register signal handlers
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


def run_migrations():
    """Runs Alembic database migrations in the current workspace context."""
    logger.info("Running database migrations...")
    try:
        # Run alembic upgrade head using the active Python environment's alembic binary
        cmd = [sys.executable, "-m", "alembic", "upgrade", "head"]
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        logger.info("Database migrations completed successfully.")
        if result.stdout:
            logger.debug(result.stdout)
    except subprocess.CalledProcessError as e:
        logger.error(f"Failed to run database migrations: {e.stderr}")
        sys.exit(1)


def main():
    parser = argparse.ArgumentParser(
        description="Professional service runner for the NavAssist backend & background workers."
    )
    parser.add_argument(
        "--host",
        type=str,
        default=os.getenv("HOST", "0.0.0.0"),
        help="Interface to bind the FastAPI server to (default: 0.0.0.0)"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.getenv("PORT", "8000")),
        help="Port to run the FastAPI server on (default: 8000)"
    )
    parser.add_argument(
        "--workers",
        type=int,
        default=int(os.getenv("WEB_WORKERS", "1")),
        help="Number of web server worker processes (default: 1)"
    )
    parser.add_argument(
        "--no-celery",
        action="store_true",
        help="Disable launching the Celery worker process"
    )
    parser.add_argument(
        "--migrate",
        action="store_true",
        help="Run database migrations before launching the application services"
    )
    parser.add_argument(
        "--reload",
        action="store_true",
        help="Enable uvicorn auto-reload for local development"
    )

    args = parser.parse_args()

    logger.info("Starting NavAssist services initialization sequence...")

    # Optional migrations run
    if args.migrate:
        run_migrations()

    # Determine command paths
    py_executable = sys.executable

    # 1. Spawn Celery Worker (if not disabled)
    if not args.no_celery:
        celery_cmd = [
            py_executable, "-m", "celery", "-A", "app.core.celery_app", "worker", "--loglevel=info"
        ]
        
        # Windows requires pool=solo configuration to function properly without fork issues
        if platform.system() == "Windows":
            celery_cmd.extend(["-P", "solo"])
            
        logger.info(f"Launching Celery worker process: {' '.join(celery_cmd)}")
        try:
            celery_proc = subprocess.Popen(
                celery_cmd,
                stdout=sys.stdout,
                stderr=sys.stderr
            )
            child_processes.append(celery_proc)
        except Exception as e:
            logger.error(f"Failed to start Celery worker: {e}")
            sys.exit(1)

    # 2. Spawn FastAPI Web Server via Uvicorn
    uvicorn_cmd = [
        py_executable, "-m", "uvicorn", "app.main:app",
        "--host", args.host,
        "--port", str(args.port),
        "--workers", str(args.workers)
    ]
    if args.reload:
        uvicorn_cmd.append("--reload")

    logger.info(f"Launching FastAPI web application: {' '.join(uvicorn_cmd)}")
    try:
        web_proc = subprocess.Popen(
            uvicorn_cmd,
            stdout=sys.stdout,
            stderr=sys.stderr
        )
        child_processes.append(web_proc)
    except Exception as e:
        logger.error(f"Failed to start FastAPI uvicorn server: {e}")
        # Clean up any already spawned workers
        signal_handler(None, None)
        sys.exit(1)

    logger.info(f"All services launched successfully. Web server running at http://{args.host}:{args.port}")
    logger.info("Press Ctrl+C to terminate all services.")

    # 3. Main process monitoring loop
    while True:
        try:
            # Check health of sub-processes
            for proc in child_processes:
                ret_code = proc.poll()
                if ret_code is not None:
                    logger.error(f"Subprocess with PID {proc.pid} exited unexpectedly with code {ret_code}.")
                    # Trigger shutdown of all other processes
                    signal_handler(None, None)
            time.sleep(1)
        except (KeyboardInterrupt, SystemExit):
            signal_handler(None, None)


if __name__ == "__main__":
    main()
