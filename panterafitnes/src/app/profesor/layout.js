import RoleGuard from "@/components/layout/RoleGuard";
import { ROLES } from "@/data/constants";

export default function ProfesorLayout({ children }) {
  return <RoleGuard allowedRole={ROLES.PROFESOR}>{children}</RoleGuard>;
}
