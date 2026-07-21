import { create } from "zustand"

interface UiState {
  theme: "light" | "dark"
  activeSheetId: string | null
  activeDialogId: string | null
  unreadNotificationCount: number
  setTheme: (theme: "light" | "dark") => void
  openSheet: (id: string) => void
  closeSheet: () => void
  openDialog: (id: string) => void
  closeDialog: () => void
  setUnreadNotificationCount: (count: number) => void
}

export const useUiStore = create<UiState>((set) => ({
  theme: "dark", // default theme
  activeSheetId: null,
  activeDialogId: null,
  unreadNotificationCount: 0,
  setTheme: (theme) => set({ theme }),
  openSheet: (id) => set({ activeSheetId: id }),
  closeSheet: () => set({ activeSheetId: null }),
  openDialog: (id) => set({ activeDialogId: id }),
  closeDialog: () => set({ activeDialogId: null }),
  setUnreadNotificationCount: (unreadNotificationCount) => set({ unreadNotificationCount })
}))

export default useUiStore
