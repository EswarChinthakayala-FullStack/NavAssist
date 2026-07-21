import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import logging
from app.core.config import settings

logger = logging.getLogger(__name__)

class EmailService:
    @staticmethod
    def send_verification_email(to_email: str, code: str):
        """Sends a KYC/auth verification email code via configured SMTP host."""
        if not settings.SMTP_HOST or not settings.SMTP_USER:
            logger.warning("SMTP email credentials are not configured. Skipping email.")
            return False

        try:
            msg = MIMEMultipart()
            msg["From"] = settings.SMTP_FROM
            msg["To"] = to_email
            msg["Subject"] = "Verify your NavAssist Email Address"

            html = f"""
            <html>
                <body style="font-family: sans-serif; padding: 20px; color: #1e293b;">
                    <div style="max-width: 500px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);">
                        <h2 style="color: #2e6fa3; margin-top: 0;">Verify your Email</h2>
                        <p>Thank you for registering on <strong>NavAssist</strong>! Use the verification code below to activate your account:</p>
                        <div style="font-size: 32px; font-weight: 800; letter-spacing: 4px; text-align: center; color: #2e6fa3; margin: 24px 0; padding: 12px; background-color: #f1f5f9; border-radius: 8px;">
                            {code}
                        </div>
                        <p style="font-size: 12px; color: #64748b;">This code will expire in 15 minutes. If you did not request this verification, please ignore this email.</p>
                    </div>
                </body>
            </html>
            """
            msg.attach(MIMEText(html, "html"))

            with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT) as server:
                server.starttls()
                server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
                server.sendmail(settings.SMTP_FROM, to_email, msg.as_string())
            
            logger.info(f"Verification email successfully sent to {to_email}")
            return True
        except Exception as e:
            logger.error(f"Failed to send verification email to {to_email}: {e}")
            return False
