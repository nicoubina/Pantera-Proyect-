"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { modulesService } from "@/services/modulesService";
import { useAppData } from "@/context/AppDataContext";

export default function useModule(resource) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [pending, setPending] = useState(false);
  const lock = useRef(false);
  const version = useRef(0);
  const { refresh } = useAppData();
  const load = useCallback(async () => {
    const current = ++version.current;
    setLoading(true);
    try {
      const result = await modulesService.list(resource);
      if (current === version.current) {
        setData(result);
        setError("");
      }
    } catch (e) {
      if (current === version.current) setError(e.message);
    } finally {
      if (current === version.current) setLoading(false);
    }
  }, [resource]);
  useEffect(() => {
    load();
    return () => {
      version.current++;
    };
  }, [load]);
  async function act(action, message = "Cambios guardados.") {
    if (lock.current) return null;
    lock.current = true;
    setPending(true);
    setError("");
    setSuccess("");
    try {
      const result = await action();
      setSuccess(message);
      await load();
      await refresh();
      return result ?? true;
    } catch (e) {
      setError(e.message);
      return null;
    } finally {
      lock.current = false;
      setPending(false);
    }
  }
  return { data, loading, error, success, pending, load, act };
}
