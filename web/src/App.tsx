import React, { Component } from "react"
import type { ErrorInfo, ReactNode } from "react"
import { RouterProvider } from "react-router-dom"
import { router } from "@/app/router"
import { NotFoundPage } from "@/pages/NotFoundPage"

interface Props {
  children?: ReactNode
}

interface State {
  hasError: boolean
}

class GlobalErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  }

  public static getDerivedStateFromError(_: Error): State {
    return { hasError: true }
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Global uncaught exception captured:", error, errorInfo)
  }

  public render() {
    if (this.state.hasError) {
      return <NotFoundPage />
    }

    return this.props.children
  }
}

export default function App() {
  return (
    <GlobalErrorBoundary>
      <RouterProvider router={router} />
    </GlobalErrorBoundary>
  )
}
