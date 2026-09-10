import DashboardHome from "@/components/common/DashboardHome";
import { ROLES } from "@/data/constants";

export default function AdminHomePage() {
  return <DashboardHome role={ROLES.ADMINISTRADOR} />;
}
