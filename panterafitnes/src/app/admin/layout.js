import RoleGuard from "@/components/layout/RoleGuard";
import { ROLES } from "@/data/mockUsers";

export default function AdminLayout({ children }) {
  return <RoleGuard allowedRole={ROLES.ADMINISTRADOR}>{children}</RoleGuard>;
}
