import DashboardHome from "@/components/common/DashboardHome";
import { ROLES } from "@/data/constants";

export default function ClienteHomePage() {
  return <DashboardHome role={ROLES.CLIENTE} />;
}
