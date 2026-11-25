import QrScanner from '@/components/qr/QrScanner';
import PageHeader from '@/components/common/PageHeader';
export default function Page() { return <div className='stack'><PageHeader title='Escanear QR' description='Registrá el ingreso de los socios leyendo el QR de su credencial.' /><QrScanner /></div>; }
