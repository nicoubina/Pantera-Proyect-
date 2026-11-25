"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import jsQR from "jsqr";
import { qrService } from "@/services/qrService";
import { useAppData } from "@/context/AppDataContext";

const PAUSE_AFTER_READ_MS = 3500;

function timeLabel(value) {
  return value ? value.slice(11, 16) : "";
}

/**
 * Control de acceso: lee el QR del socio con la cámara (o un lector USB / código escrito)
 * y registra el ingreso en el servidor. La hora la pone el backend.
 */
export default function QrScanner() {
  const { refresh } = useAppData();
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const frameRef = useRef(0);
  const busyRef = useRef(false);
  const lastCodeRef = useRef({ code: "", at: 0 });
  const [cameraOn, setCameraOn] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [manualCode, setManualCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);

  const register = useCallback(async (rawCode) => {
    const codigo = String(rawCode || "").trim();
    if (!codigo || busyRef.current) return;
    busyRef.current = true;
    setBusy(true);
    try {
      const response = await qrService.registerEntry({ codigo });
      const ok = response.estadoAsistencia === "ASISTIDA";
      setResult({ tone: ok ? "success" : "error", icon: ok ? "check_circle" : "schedule", message: response.mensaje });
      setHistory((items) => [{ ...response, ok }, ...items].slice(0, 8));
      refresh();
    } catch (error) {
      setResult({ tone: "error", icon: "block", message: error.message || "No se pudo registrar el ingreso." });
    } finally {
      setTimeout(() => { busyRef.current = false; setBusy(false); }, PAUSE_AFTER_READ_MS);
    }
  }, [refresh]);

  const stopCamera = useCallback(() => {
    cancelAnimationFrame(frameRef.current);
    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;
    if (videoRef.current) videoRef.current.srcObject = null;
    setCameraOn(false);
  }, []);

  const scanFrame = useCallback(() => {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas || !streamRef.current) return;
    if (video.readyState === video.HAVE_ENOUGH_DATA && !busyRef.current) {
      const width = video.videoWidth;
      const height = video.videoHeight;
      canvas.width = width;
      canvas.height = height;
      const context = canvas.getContext("2d", { willReadFrequently: true });
      context.drawImage(video, 0, 0, width, height);
      const found = jsQR(context.getImageData(0, 0, width, height).data, width, height, { inversionAttempts: "dontInvert" });
      const now = Date.now();
      if (found?.data && !(found.data === lastCodeRef.current.code && now - lastCodeRef.current.at < 8000)) {
        lastCodeRef.current = { code: found.data, at: now };
        register(found.data);
      }
    }
    frameRef.current = requestAnimationFrame(scanFrame);
  }, [register]);

  async function startCamera() {
    setCameraError("");
    if (!navigator.mediaDevices?.getUserMedia) {
      setCameraError("Este navegador no permite usar la cámara. Usá el ingreso por código.");
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment" }, audio: false });
      streamRef.current = stream;
      const video = videoRef.current;
      video.srcObject = stream;
      video.setAttribute("playsinline", "true");
      await video.play();
      setCameraOn(true);
      frameRef.current = requestAnimationFrame(scanFrame);
    } catch (error) {
      setCameraError(error?.name === "NotAllowedError"
        ? "Permiso de cámara denegado. Habilitalo en el navegador o usá el ingreso por código."
        : "No se encontró una cámara disponible. Usá el ingreso por código.");
      stopCamera();
    }
  }

  useEffect(() => stopCamera, [stopCamera]);

  useEffect(() => {
    if (!result) return undefined;
    const timer = setTimeout(() => setResult(null), 6000);
    return () => clearTimeout(timer);
  }, [result]);

  function submitManual(event) {
    event.preventDefault();
    register(manualCode);
    setManualCode("");
  }

  return (
    <div className="stack">
      <section className="panel scanner-panel">
        <div className="scanner-view">
          <video ref={videoRef} className={`scanner-video ${cameraOn ? "on" : ""}`} muted playsInline />
          {!cameraOn && (
            <div className="scanner-placeholder">
              <span className="material-symbols-outlined" style={{ fontSize: 56 }}>qr_code_scanner</span>
              <p className="muted">La cámara está apagada</p>
            </div>
          )}
          {cameraOn && <span className="scanner-frame" aria-hidden="true" />}
          <canvas ref={canvasRef} hidden />
        </div>

        <div className="stack" style={{ gap: 12 }}>
          <div>
            <p className="eyebrow">Control de acceso</p>
            <h3>Escaneá el QR del socio</h3>
            <p className="muted">
              Apuntá la cámara al QR que el socio muestra en su celular. El sistema busca su reserva
              confirmada para la clase de este horario y registra el ingreso con la hora actual.
            </p>
          </div>
          <div className="actions-row">
            {cameraOn ? (
              <button className="secondary-button" type="button" onClick={stopCamera}>
                <span className="material-symbols-outlined" style={{ verticalAlign: "middle", marginRight: 6 }}>videocam_off</span>
                Apagar cámara
              </button>
            ) : (
              <button className="primary-button" type="button" onClick={startCamera}>
                <span className="material-symbols-outlined" style={{ verticalAlign: "middle", marginRight: 6 }}>photo_camera</span>
                Activar cámara
              </button>
            )}
          </div>
          {cameraError && <p className="inline-warning">{cameraError}</p>}

          <form className="manual-code" onSubmit={submitManual}>
            <label htmlFor="codigo-qr">Código del socio (lector USB o escrito a mano)</label>
            <div className="actions-row">
              <input
                id="codigo-qr"
                value={manualCode}
                onChange={(event) => setManualCode(event.target.value)}
                placeholder="PF-..."
                autoComplete="off"
              />
              <button className="secondary-button" type="submit" disabled={!manualCode.trim() || busy}>Registrar</button>
            </div>
          </form>

          <div className={`result-banner ${result ? `visible ${result.tone}` : ""}`} role="status" aria-live="polite">
            {result ? (
              <>
                <span className="material-symbols-outlined">{result.icon}</span>
                <span>{result.message}</span>
              </>
            ) : null}
          </div>
        </div>
      </section>

      <section className="panel">
        <h3>Últimos ingresos</h3>
        {history.length ? (
          <div className="stack" style={{ gap: 8, marginTop: 12 }}>
            {history.map((item) => (
              <article className="reservation-card" key={`${item.reservaId}-${item.horaIngreso}`}>
                <div>
                  <h3>{item.cliente}</h3>
                  <p className="muted">{item.clase} · {timeLabel(item.horaIngreso)} hs</p>
                </div>
                <span className={`status-pill ${item.ok ? "success" : "danger"}`}>{item.ok ? "ASISTIDA" : "AUSENTE"}</span>
              </article>
            ))}
          </div>
        ) : (
          <p className="muted" style={{ marginTop: 8 }}>Todavía no se registraron ingresos en esta sesión.</p>
        )}
      </section>
    </div>
  );
}
