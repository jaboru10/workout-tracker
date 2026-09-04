/*
 * Seed de rutinas predefinidas (IL-004) + migración de datos actuales.
 *
 * CÓMO EJECUTAR (mongosh):
 *   mongosh "<TU_CONNECTION_STRING_DE_ATLAS>" scripts/seed_routines.js
 * o pegarlo en la pestaña "mongosh" de Atlas (Data Services > Connect > Shell),
 * ya conectado a la base `workout`.
 *
 * QUÉ HACE:
 *   1. (Re)carga 10 rutinas predefinidas famosas en la colección `routines`
 *      (userId=null, preset=true). Son intocables: al "usarlas" desde la app se
 *      copian a una rutina personal. Re-ejecutar el script las refresca.
 *   2. MIGRACIÓN idempotente para TU usuario: si no tienes ninguna rutina propia,
 *      crea "Mi rutina" (activa) y engancha a ella tus días de entrenamiento
 *      actuales que no tuvieran rutina. Las sesiones antiguas NO se tocan
 *      (quedan "sin rutina" en el historial, según lo decidido).
 *
 * AJUSTA ESTO antes de ejecutar:
 */
const USER_ID = "6a8e2304a1d5ad7521ed45e9"; // Atlas (prod). Para local: "6a8e0e378a790e449b58cf0b"
const CLASS = "com.javier.workout.model.Routine";

// Helper compacto para un ejercicio de plantilla.
// e(nombre, grupo, series, reps, [bodyweight])
function e(name, muscleGroup, targetSets, targetReps, bodyweight) {
  return { name, muscleGroup, bodyweight: !!bodyweight, targetSets, targetReps, notes: "" };
}
// Helper para un día.
function d(order, name, exercises) {
  return { name, order, exercises };
}
// Helper para una rutina predefinida. Jerarquía: Nivel > Tipo > Rutina.
function preset(name, level, type, templateDays) {
  return {
    userId: null, name, level, type,
    active: false, preset: true, archived: false, sourceRoutineId: null,
    templateDays, _class: CLASS,
  };
}

const PRESETS = [
  preset("Full Body 3 días", "Principiante", "Fuerza", [
    d(1, "Día A", [
      e("Sentadilla", "Pierna", 3, "5"), e("Press banca", "Pecho", 3, "5"),
      e("Remo con barra", "Espalda", 3, "5"), e("Press militar", "Hombro", 3, "8"),
      e("Curl bíceps", "Bíceps", 2, "10"),
    ]),
    d(2, "Día B", [
      e("Peso muerto", "Pierna", 1, "5"), e("Press militar", "Hombro", 3, "5"),
      e("Jalón al pecho", "Espalda", 3, "8"), e("Press inclinado", "Pecho", 3, "8"),
      e("Extensión tríceps", "Tríceps", 2, "10"),
    ]),
    d(3, "Día C", [
      e("Sentadilla", "Pierna", 3, "5"), e("Press banca", "Pecho", 3, "5"),
      e("Remo con barra", "Espalda", 3, "5"), e("Elevaciones laterales", "Hombro", 3, "12"),
      e("Plancha", "Core", 3, "60s", true),
    ]),
  ]),

  preset("StrongLifts 5x5", "Principiante", "Fuerza", [
    d(1, "Entreno A", [
      e("Sentadilla", "Pierna", 5, "5"), e("Press banca", "Pecho", 5, "5"),
      e("Remo con barra", "Espalda", 5, "5"),
    ]),
    d(2, "Entreno B", [
      e("Sentadilla", "Pierna", 5, "5"), e("Press militar", "Hombro", 5, "5"),
      e("Peso muerto", "Pierna", 1, "5"),
    ]),
  ]),

  preset("Starting Strength", "Principiante", "Fuerza", [
    d(1, "Día A", [
      e("Sentadilla", "Pierna", 3, "5"), e("Press banca", "Pecho", 3, "5"),
      e("Peso muerto", "Pierna", 1, "5"),
    ]),
    d(2, "Día B", [
      e("Sentadilla", "Pierna", 3, "5"), e("Press militar", "Hombro", 3, "5"),
      e("Peso muerto", "Pierna", 1, "5"),
    ]),
  ]),

  preset("Madcow 5x5", "Intermedio", "Fuerza", [
    d(1, "Pesado (lunes)", [
      e("Sentadilla", "Pierna", 5, "5"), e("Press banca", "Pecho", 5, "5"),
      e("Remo con barra", "Espalda", 5, "5"),
    ]),
    d(2, "Ligero (miércoles)", [
      e("Sentadilla", "Pierna", 4, "5"), e("Press militar", "Hombro", 4, "5"),
      e("Peso muerto", "Pierna", 4, "5"),
    ]),
    d(3, "PR (viernes)", [
      e("Sentadilla", "Pierna", 4, "5"), e("Press banca", "Pecho", 4, "5"),
      e("Remo con barra", "Espalda", 4, "5"),
    ]),
  ]),

  preset("Upper/Lower 4 días", "Intermedio", "Híbrido", [
    d(1, "Superior A", [
      e("Press banca", "Pecho", 4, "6-8"), e("Remo con barra", "Espalda", 4, "6-8"),
      e("Press militar", "Hombro", 3, "8-10"), e("Jalón al pecho", "Espalda", 3, "10"),
      e("Curl bíceps", "Bíceps", 3, "12"), e("Extensión tríceps", "Tríceps", 3, "12"),
    ]),
    d(2, "Inferior A", [
      e("Sentadilla", "Pierna", 4, "6-8"), e("Peso muerto rumano", "Pierna", 3, "8-10"),
      e("Prensa", "Pierna", 3, "10-12"), e("Curl femoral", "Pierna", 3, "12"),
      e("Gemelo de pie", "Pierna", 4, "15"),
    ]),
    d(3, "Superior B", [
      e("Press inclinado", "Pecho", 4, "8-10"), e("Remo con mancuerna", "Espalda", 4, "10"),
      e("Elevaciones laterales", "Hombro", 4, "12-15"), e("Face pull", "Hombro", 3, "15"),
      e("Curl martillo", "Bíceps", 3, "12"), e("Fondos", "Tríceps", 3, "10", true),
    ]),
    d(4, "Inferior B", [
      e("Peso muerto", "Pierna", 3, "5"), e("Zancadas", "Pierna", 3, "10"),
      e("Prensa", "Pierna", 4, "12"), e("Extensión de cuádriceps", "Pierna", 3, "15"),
      e("Gemelo sentado", "Pierna", 4, "20"),
    ]),
  ]),

  preset("Push/Pull/Legs (6 días)", "Intermedio", "Hipertrofia", [
    d(1, "Empuje", [
      e("Press banca", "Pecho", 4, "8"), e("Press militar", "Hombro", 4, "10"),
      e("Press inclinado mancuerna", "Pecho", 3, "10"), e("Elevaciones laterales", "Hombro", 4, "15"),
      e("Extensión tríceps polea", "Tríceps", 3, "12"), e("Fondos", "Tríceps", 3, "12", true),
    ]),
    d(2, "Tirón", [
      e("Dominadas", "Espalda", 4, "8", true), e("Remo con barra", "Espalda", 4, "8"),
      e("Jalón al pecho", "Espalda", 3, "10"), e("Face pull", "Hombro", 3, "15"),
      e("Curl bíceps barra", "Bíceps", 4, "10"), e("Curl martillo", "Bíceps", 3, "12"),
    ]),
    d(3, "Pierna", [
      e("Sentadilla", "Pierna", 4, "8"), e("Peso muerto rumano", "Pierna", 3, "10"),
      e("Prensa", "Pierna", 4, "12"), e("Curl femoral", "Pierna", 3, "12"),
      e("Extensión de cuádriceps", "Pierna", 3, "15"), e("Gemelo de pie", "Pierna", 4, "20"),
    ]),
  ]),

  preset("Weider 5 días", "Intermedio", "Hipertrofia", [
    d(1, "Pecho", [
      e("Press banca", "Pecho", 4, "10"), e("Press inclinado mancuerna", "Pecho", 4, "10"),
      e("Aperturas", "Pecho", 3, "12"), e("Fondos", "Tríceps", 3, "12", true),
    ]),
    d(2, "Espalda", [
      e("Dominadas", "Espalda", 4, "8", true), e("Remo con barra", "Espalda", 4, "10"),
      e("Jalón al pecho", "Espalda", 3, "12"), e("Remo con mancuerna", "Espalda", 3, "10"),
      e("Peso muerto", "Pierna", 3, "8"),
    ]),
    d(3, "Hombro", [
      e("Press militar", "Hombro", 4, "10"), e("Elevaciones laterales", "Hombro", 4, "15"),
      e("Elevaciones frontales", "Hombro", 3, "12"), e("Face pull", "Hombro", 3, "15"),
      e("Pájaros", "Hombro", 3, "15"),
    ]),
    d(4, "Brazo", [
      e("Curl bíceps barra", "Bíceps", 4, "10"), e("Curl martillo", "Bíceps", 3, "12"),
      e("Curl concentrado", "Bíceps", 3, "12"), e("Extensión tríceps polea", "Tríceps", 4, "12"),
      e("Press francés", "Tríceps", 3, "10"),
    ]),
    d(5, "Pierna", [
      e("Sentadilla", "Pierna", 4, "10"), e("Prensa", "Pierna", 4, "12"),
      e("Extensión de cuádriceps", "Pierna", 3, "15"), e("Curl femoral", "Pierna", 3, "12"),
      e("Gemelo de pie", "Pierna", 4, "20"),
    ]),
  ]),

  preset("Arnold Split (6 días)", "Avanzado", "Hipertrofia", [
    d(1, "Pecho/Espalda", [
      e("Press banca", "Pecho", 4, "10"), e("Press inclinado", "Pecho", 4, "10"),
      e("Dominadas", "Espalda", 4, "8", true), e("Remo con barra", "Espalda", 4, "10"),
      e("Aperturas", "Pecho", 3, "12"), e("Pullover", "Espalda", 3, "12"),
    ]),
    d(2, "Hombro/Brazo", [
      e("Press militar", "Hombro", 4, "10"), e("Elevaciones laterales", "Hombro", 4, "15"),
      e("Curl bíceps barra", "Bíceps", 4, "10"), e("Press francés", "Tríceps", 4, "10"),
      e("Curl martillo", "Bíceps", 3, "12"), e("Extensión tríceps polea", "Tríceps", 3, "12"),
    ]),
    d(3, "Pierna", [
      e("Sentadilla", "Pierna", 4, "10"), e("Prensa", "Pierna", 4, "12"),
      e("Peso muerto rumano", "Pierna", 3, "10"), e("Extensión de cuádriceps", "Pierna", 3, "15"),
      e("Curl femoral", "Pierna", 3, "12"), e("Gemelo de pie", "Pierna", 4, "20"),
    ]),
  ]),

  preset("PHUL 4 días", "Intermedio", "Híbrido", [
    d(1, "Superior fuerza", [
      e("Press banca", "Pecho", 4, "5"), e("Remo con barra", "Espalda", 4, "5"),
      e("Press militar", "Hombro", 3, "6"), e("Jalón al pecho", "Espalda", 3, "8"),
      e("Curl bíceps", "Bíceps", 3, "8"), e("Extensión tríceps", "Tríceps", 3, "8"),
    ]),
    d(2, "Inferior fuerza", [
      e("Sentadilla", "Pierna", 4, "5"), e("Peso muerto", "Pierna", 4, "5"),
      e("Prensa", "Pierna", 3, "10"), e("Curl femoral", "Pierna", 3, "10"),
      e("Gemelo de pie", "Pierna", 4, "12"),
    ]),
    d(3, "Superior hipertrofia", [
      e("Press inclinado mancuerna", "Pecho", 4, "12"), e("Remo con mancuerna", "Espalda", 4, "12"),
      e("Elevaciones laterales", "Hombro", 4, "15"), e("Aperturas", "Pecho", 3, "12"),
      e("Curl martillo", "Bíceps", 3, "12"), e("Extensión tríceps polea", "Tríceps", 3, "15"),
    ]),
    d(4, "Inferior hipertrofia", [
      e("Sentadilla frontal", "Pierna", 4, "12"), e("Peso muerto rumano", "Pierna", 4, "12"),
      e("Prensa", "Pierna", 4, "15"), e("Extensión de cuádriceps", "Pierna", 3, "15"),
      e("Curl femoral", "Pierna", 3, "15"), e("Gemelo sentado", "Pierna", 4, "20"),
    ]),
  ]),

  preset("PHAT 5 días", "Avanzado", "Híbrido", [
    d(1, "Espalda/Hombro fuerza", [
      e("Remo con barra", "Espalda", 4, "5"), e("Dominadas", "Espalda", 4, "6", true),
      e("Press militar", "Hombro", 4, "5"), e("Elevaciones laterales", "Hombro", 3, "8"),
    ]),
    d(2, "Pecho/Brazo fuerza", [
      e("Press banca", "Pecho", 4, "5"), e("Press inclinado", "Pecho", 4, "6"),
      e("Curl bíceps barra", "Bíceps", 3, "8"), e("Press francés", "Tríceps", 3, "8"),
    ]),
    d(3, "Pierna fuerza", [
      e("Sentadilla", "Pierna", 4, "5"), e("Peso muerto", "Pierna", 4, "5"),
      e("Prensa", "Pierna", 3, "10"), e("Curl femoral", "Pierna", 3, "8"),
    ]),
    d(4, "Espalda/Hombro hipertrofia", [
      e("Remo con mancuerna", "Espalda", 4, "12"), e("Jalón al pecho", "Espalda", 4, "12"),
      e("Elevaciones laterales", "Hombro", 4, "15"), e("Face pull", "Hombro", 3, "15"),
      e("Curl bíceps", "Bíceps", 4, "12"),
    ]),
    d(5, "Pecho/Brazo hipertrofia", [
      e("Press inclinado mancuerna", "Pecho", 4, "12"), e("Aperturas", "Pecho", 4, "15"),
      e("Extensión tríceps polea", "Tríceps", 4, "12"), e("Curl martillo", "Bíceps", 3, "12"),
      e("Fondos", "Tríceps", 3, "12", true),
    ]),
  ]),
];

// ---- 1) Cargar predefinidas (refresca si se re-ejecuta) ----
db.routines.deleteMany({ preset: true });
db.routines.insertMany(PRESETS);
print("Predefinidas cargadas: " + PRESETS.length);

// ---- 2) Migración de datos actuales del usuario ----
const yaTiene = db.routines.findOne({ userId: USER_ID, preset: { $ne: true }, archived: { $ne: true } });
if (yaTiene) {
  print("El usuario ya tiene rutina(s) propia(s); no se migra nada.");
} else {
  const miRutinaId = new ObjectId();
  const hex = miRutinaId.toHexString ? miRutinaId.toHexString() : miRutinaId.str;
  db.routines.insertOne({
    _id: miRutinaId,
    userId: USER_ID, name: "Mi rutina", level: null, type: null,
    active: true, preset: false, archived: false, sourceRoutineId: null,
    templateDays: [], _class: CLASS,
  });
  const res = db.training_days.updateMany(
    { userId: USER_ID, $or: [{ routineId: { $exists: false } }, { routineId: null }] },
    { $set: { routineId: hex } }
  );
  print("Creada 'Mi rutina' (activa). Días enganchados: " + res.modifiedCount);
}

print("Seed completado.");
