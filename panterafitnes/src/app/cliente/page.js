import DashboardHome from "@/components/common/DashboardHome";
import { ROLES } from "@/data/mockUsers";

export default function ClienteHomePage() {
  return <DashboardHome role={ROLES.CLIENTE} />;
}
