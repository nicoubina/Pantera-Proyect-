import QrSimulator from '@/components/qr/QrSimulator';
import PageHeader from '@/components/common/PageHeader';
export default function Page() { return <div className='stack'><PageHeader title='Mi QR' description='Credencial y simulación de ingreso a clases.' /><QrSimulator /></div>; }
