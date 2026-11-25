import MyQrCard from '@/components/qr/MyQrCard';
import PageHeader from '@/components/common/PageHeader';
export default function Page() { return <div className='stack'><PageHeader title='Mi QR' description='Tu credencial de ingreso: mostrala en recepción o al profesor al llegar a clase.' /><MyQrCard /></div>; }
