import React, { Component, type ErrorInfo, type ReactNode } from "react"
import { Button } from "@/components/ui/button"
import { WarningOctagonIcon, ArrowClockwiseIcon } from "@phosphor-icons/react"

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  }

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught Error Boundary Exception:", error, errorInfo)
  }

  private handleReset = () => {
    this.setState({ hasError: false, error: null })
    window.location.reload()
  }

  public render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback
      }

      return (
        <div className="min-h-[400px] w-full flex flex-col items-center justify-center p-6 text-center bg-card rounded-2xl border border-border/80 shadow-md space-y-4 my-6">
          <div className="w-14 h-14 rounded-2xl bg-destructive/10 text-destructive flex items-center justify-center">
            <WarningOctagonIcon size={32} weight="fill" />
          </div>

          <div className="space-y-1 max-w-md">
            <h3 className="text-lg font-bold text-foreground">Something went wrong</h3>
            <p className="text-xs text-muted-foreground leading-relaxed">
              An unexpected application error occurred while rendering this section.
            </p>
            {this.state.error && (
              <p className="text-[11px] font-mono text-destructive/80 bg-muted/40 p-2 rounded-lg mt-2 text-left truncate max-w-full">
                {this.state.error.message}
              </p>
            )}
          </div>

          <Button
            onClick={this.handleReset}
            className="bg-primary text-primary-foreground font-bold text-xs py-4 px-6 rounded-xl flex items-center gap-2 cursor-pointer shadow-sm hover:scale-[1.02] transition-transform"
          >
            <ArrowClockwiseIcon size={16} weight="bold" />
            <span>Reload & Try Again</span>
          </Button>
        </div>
      )
    }

    return this.props.children
  }
}
export default ErrorBoundary
