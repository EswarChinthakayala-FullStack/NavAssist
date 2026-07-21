"use client"

import { useLocation } from "react-router-dom"
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { Link } from "react-router-dom"

export function NavProjects({
  projects,
  label = "Quick Actions",
}: {
  label?: string
  projects: {
    name: string
    url: string
    icon: React.ReactNode
    badge?: string
  }[]
}) {
  const location = useLocation()

  return (
    <SidebarGroup className="group-data-[collapsible=icon]:hidden">
      <SidebarGroupLabel>{label}</SidebarGroupLabel>
      <SidebarMenu>
        {projects.map((item) => {
          const isActive =
            location.pathname === item.url ||
            location.pathname.startsWith(item.url + "/")

          return (
            <SidebarMenuItem key={item.name}>
              <SidebarMenuButton
                render={<Link to={item.url} />}
                className={isActive ? "bg-primary/10 text-primary font-semibold" : ""}
              >
                <span className={`shrink-0 ${isActive ? "text-primary" : ""}`}>
                  {item.icon}
                </span>
                <span>{item.name}</span>
                {item.badge && (
                  <span className="ml-auto text-[9px] font-bold uppercase tracking-widest bg-primary/15 text-primary px-1.5 py-0.5 rounded-full leading-none">
                    {item.badge}
                  </span>
                )}
              </SidebarMenuButton>
            </SidebarMenuItem>
          )
        })}
      </SidebarMenu>
    </SidebarGroup>
  )
}
