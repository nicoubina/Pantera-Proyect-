import RoleGuard from "@/components/layout/RoleGuard";
import { ROLES } from "@/data/mockUsers";

export default function ProfesorLayout({ children }) {
  return <RoleGuard allowedRole={ROLES.PROFESOR}>{children}</RoleGuard>;
}
