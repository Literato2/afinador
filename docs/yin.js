// Detector de tono YIN (de Cheveigné & Kawahara, 2002). Mismo algoritmo que la app Android.
export function createYin(sampleRate, size) {
  const half = size >> 1;
  const diff = new Float32Array(half);
  const minLag = Math.floor(sampleRate / 1000);
  const maxLag = Math.min(half - 1, Math.floor(sampleRate / 60));
  const THRESHOLD = 0.12;
  const MIN_RMS = 0.006;

  return function detect(buf) {
    let rms = 0;
    for (let i = 0; i < size; i++) rms += buf[i] * buf[i];
    if (Math.sqrt(rms / size) < MIN_RMS) return null;

    diff[0] = 1;
    let running = 0;
    for (let tau = 1; tau < half; tau++) {
      let sum = 0;
      for (let i = 0; i < half; i++) {
        const d = buf[i] - buf[i + tau];
        sum += d * d;
      }
      running += sum;
      diff[tau] = running === 0 ? 1 : (sum * tau) / running;
    }

    let tau = minLag;
    for (; tau < maxLag; tau++) {
      if (diff[tau] < THRESHOLD) {
        while (tau + 1 < maxLag && diff[tau + 1] < diff[tau]) tau++;
        break;
      }
    }
    if (tau >= maxLag || diff[tau] >= THRESHOLD) return null;

    let better = tau;
    if (tau > 0 && tau < half - 1) {
      const s0 = diff[tau - 1], s1 = diff[tau], s2 = diff[tau + 1];
      const denom = 2 * (2 * s1 - s2 - s0);
      if (denom !== 0) better = tau + (s2 - s0) / denom;
    }
    return sampleRate / better;
  };
}

// De la 6ª (grave) a la 1ª (aguda).
export const INSTRUMENTS = {
  guitarra: {
    label: "Guitarra",
    strings: [
      { n: 6, name: "E", oct: 2, hz: 82.41 },
      { n: 5, name: "A", oct: 2, hz: 110.0 },
      { n: 4, name: "D", oct: 3, hz: 146.83 },
      { n: 3, name: "G", oct: 3, hz: 196.0 },
      { n: 2, name: "B", oct: 3, hz: 246.94 },
      { n: 1, name: "E", oct: 4, hz: 329.63 },
    ],
  },
  bandurria: {
    label: "Bandurria",
    strings: [
      { n: 6, name: "G♯", oct: 3, hz: 207.65 },
      { n: 5, name: "C♯", oct: 4, hz: 277.18 },
      { n: 4, name: "F♯", oct: 4, hz: 369.99 },
      { n: 3, name: "B", oct: 4, hz: 493.88 },
      { n: 2, name: "E", oct: 5, hz: 659.26 },
      { n: 1, name: "A", oct: 5, hz: 880.0 },
    ],
  },
  // Cualquier nota: se calcula la más cercana (temperado, La4 = 440 Hz).
  cromatico: { label: "Cromático", strings: [] },
};

const NAMES = ["C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"];
const noteCache = new Map();

// Nota temperada más cercana; n = 0 porque no es una cuerda. Cacheada para que
// la misma nota sea el mismo objeto (la UI compara por identidad).
export function nearestNote(hz) {
  const midi = Math.round(69 + 12 * Math.log2(hz / 440));
  if (!noteCache.has(midi)) {
    noteCache.set(midi, { n: 0, name: NAMES[((midi % 12) + 12) % 12], oct: Math.floor(midi / 12) - 1, hz: 440 * 2 ** ((midi - 69) / 12) });
  }
  return noteCache.get(midi);
}

export const initialTarget = (key) => INSTRUMENTS[key].strings[0] ?? nearestNote(440);

export const cents = (hz, target) => 1200 * Math.log2(hz / target);

export function closest(hz, strings) {
  if (!strings.length) return nearestNote(hz);
  return strings.reduce((a, b) => (Math.abs(cents(hz, b.hz)) < Math.abs(cents(hz, a.hz)) ? b : a));
}
