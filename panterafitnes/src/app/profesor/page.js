import DashboardHome from "@/components/common/DashboardHome";
import { ROLES } from "@/data/mockUsers";

export default function ProfesorHomePage() {
  return <DashboardHome role={ROLES.PROFESOR} />;
}
