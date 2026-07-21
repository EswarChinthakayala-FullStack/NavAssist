import React from "react"
import { QueryClientProvider } from "@tanstack/react-query"
import { ThemeProvider as NextThemesProvider } from "next-themes"
import { queryClient } from "./query-client"
import { AuthProvider } from "@/store/auth-context"
import { Toaster } from "@/components/ui/sonner"

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <NextThemesProvider attribute="class" defaultTheme="light" enableSystem={false}>
        <AuthProvider>
          {children}
          <Toaster />
        </AuthProvider>
      </NextThemesProvider>
    </QueryClientProvider>
  )
}
export default Providers
