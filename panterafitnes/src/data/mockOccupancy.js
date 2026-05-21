export const CAPACIDAD_TOTAL_GIMNASIO = 60;

export const initialOccupancy = {
  updatedAt: new Date().toISOString(),
  total: {
    nombre: "Gimnasio completo",
    capacidad: CAPACIDAD_TOTAL_GIMNASIO,
    personas: 32
  },
  sectores: [
    {
      id: "sala-musculacion",
      nombre: "Sala de musculacion",
      capacidad: 40,
      personas: 22
    },
    {
      id: "sala-clases",
      nombre: "Sala de clases",
      capacidad: 20,
      personas: 10
    }
  ]
};
