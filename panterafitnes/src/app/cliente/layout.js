import RoleGuard from "@/components/layout/RoleGuard";
import { ROLES } from "@/data/mockUsers";

export default function ClienteLayout({ children }) {
  return <RoleGuard allowedRole={ROLES.CLIENTE}>{children}</RoleGuard>;
}
