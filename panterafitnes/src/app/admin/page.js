import DashboardHome from "@/components/common/DashboardHome";
import { ROLES } from "@/data/mockUsers";

export default function AdminHomePage() {
  return <DashboardHome role={ROLES.ADMINISTRADOR} />;
}
