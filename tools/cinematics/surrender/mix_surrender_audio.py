# Banda sonora de la cinematica "Rendirse" (FDAF). Mezcla sonidos que ya estan en el repo con efectos sintetizados aqui
# (clics de los resortes del traje, servos de las cabezas, latido apagado, zumbido y chasquidos electricos), de modo que
# no entra ningun audio de procedencia desconocida. Salida: WAV estereo 44.1 kHz 16 bit.
# Uso: python mix_surrender_audio.py <carpeta audio de FDAF> <salida.wav>
import sys, wave
import numpy as np

SR = 44100
AUD, OUT = sys.argv[1], sys.argv[2]
DUR = 12.6
mix = np.zeros((int(DUR * SR), 2), dtype=np.float64)
rng = np.random.default_rng(1987)

# Marcas de tiempo (segundos desde que se completa la barra de Rendirse). Deben coincidir con
# PnlCinematicaRendirse (INICIO_FOTOGRAMAS_MS, FPS) y con la direccion de Blender (fotogramas a 20 fps).
T_FOTOS = 2.2           # primer fotograma renderizado
FPS = 20.0
def tf(frame):          # instante del fotograma f (1..160) de Blender
    return T_FOTOS + (frame - 1) / FPS


def leer(ruta):
    w = wave.open(ruta)
    n, ch, sr, sw = w.getnframes(), w.getnchannels(), w.getframerate(), w.getsampwidth()
    d = np.frombuffer(w.readframes(n), dtype=np.int16 if sw == 2 else np.uint8).astype(np.float64)
    d = d / 32768.0 if sw == 2 else (d - 128) / 128.0
    d = d.reshape(-1, ch).mean(axis=1)
    if sr != SR:  # remuestreo lineal
        x = np.linspace(0, len(d) - 1, int(len(d) * SR / sr))
        d = np.interp(x, np.arange(len(d)), d)
    return d


def pasa_bajos(x, corte):
    a = np.exp(-2 * np.pi * corte / SR); y = np.zeros_like(x); acc = 0.0
    for i in range(len(x)):
        acc = (1 - a) * x[i] + a * acc; y[i] = acc
    return y


def poner(señal, t, gan=1.0, pan=0.0, fade_in=0.0, fade_out=0.0):
    s = señal.copy()
    if fade_in > 0:
        k = min(len(s), int(fade_in * SR)); s[:k] *= np.linspace(0, 1, k)
    if fade_out > 0:
        k = min(len(s), int(fade_out * SR)); s[-k:] *= np.linspace(1, 0, k)
    i0 = int(t * SR); i1 = min(len(mix), i0 + len(s)); s = s[:i1 - i0]
    izq = gan * np.sqrt((1 - pan) / 2); der = gan * np.sqrt((1 + pan) / 2)
    mix[i0:i1, 0] += s * izq; mix[i0:i1, 1] += s * der


def env(n, ataque, caida):
    t = np.arange(n) / SR
    return np.minimum(1, t / max(ataque, 1e-4)) * np.exp(-t / caida)


# 1) Apagon de la oficina: el principio real de power_down.wav (el resto es un zumbido largo que no se usa)
pd = leer(AUD + '/game_over/power_down.wav')[:int(1.9 * SR)]
poner(pd, 0.0, 0.9, 0.0, fade_out=0.5)

# 2) En la oscuridad: golpe sordo + clics de los resortes del traje cerrandose (sintesis: rafaga de ruido + resonancias metalicas)
def clic(intensidad=1.0):
    n = int(0.18 * SR); t = np.arange(n) / SR
    ruido = rng.standard_normal(n) * env(n, 0.0005, 0.006)
    metal = sum(np.sin(2 * np.pi * f * t + rng.uniform(0, 6)) * env(n, 0.0003, d) * a
                for f, d, a in ((2310, 0.03, 0.5), (3870, 0.02, 0.35), (5120, 0.012, 0.25), (1460, 0.05, 0.3)))
    return intensidad * (ruido * 0.8 + metal)

def golpe():
    n = int(0.6 * SR); t = np.arange(n) / SR
    return np.sin(2 * np.pi * (55 * np.exp(-t * 3)) * t) * env(n, 0.002, 0.15) + pasa_bajos(rng.standard_normal(n), 300) * env(n, 0.001, 0.05) * 2

poner(golpe(), 1.25, 0.9)
for i, t in enumerate((1.45, 1.62, 1.74, 1.83, 1.90, 1.95, 1.99)):
    poner(clic(0.55 + 0.06 * i), t, 0.5, pan=rng.uniform(-0.5, 0.5))

# 3) Dentro del traje: respiracion agitada amortiguada (pasa bajos = el sonido llega a traves de la cabeza del traje)
resp = pasa_bajos(leer(AUD + '/sfx/heavy_breathing.wav'), 900)
for k in range(3):
    poner(resp, 1.7 + k * resp.size / SR * 0.98, 0.55 - 0.12 * k, 0.0, fade_in=0.3 if k == 0 else 0.05, fade_out=0.3)

# 4) Zumbido grave continuo (drone) + latido apagado que se acelera
n = int((DUR - T_FOTOS) * SR); t = np.arange(n) / SR
drone = (np.sin(2 * np.pi * 41 * t) * 0.5 + np.sin(2 * np.pi * 61.5 * t + 1) * 0.25 + pasa_bajos(rng.standard_normal(n), 120) * 1.2)
drone *= np.minimum(1, t / 1.5) * 0.22
poner(drone, T_FOTOS - 0.2, 1.0, fade_out=1.2)

def latido():
    n = int(0.35 * SR); tt = np.arange(n) / SR
    golpe1 = np.sin(2 * np.pi * 52 * tt) * env(n, 0.004, 0.06)
    golpe2 = np.sin(2 * np.pi * 47 * tt) * env(n, 0.004, 0.05)
    out = golpe1.copy(); d = int(0.17 * SR); out[d:] += golpe2[:n - d] * 0.7
    return out
tl = T_FOTOS + 0.2; bpm = 68.0
while tl < tf(150):
    poner(latido(), tl, 0.95)
    bpm = min(128.0, bpm * 1.045); tl += 60.0 / bpm

# 5) Servos de las cabezas al girar hacia el jugador (Bonnie a la derecha, Chica a la izquierda; Freddy al centro)
def servo(dur, f0, f1):
    n = int(dur * SR); tt = np.arange(n) / SR
    f = np.linspace(f0, f1, n); fase = 2 * np.pi * np.cumsum(f) / SR
    sierra = 2 * ((fase / (2 * np.pi)) % 1) - 1
    s = pasa_bajos(sierra, 1800) * 0.6 + np.sin(fase * 2) * 0.15
    s *= np.minimum(1, tt / 0.05) * np.minimum(1, (dur - tt) / 0.12)
    s *= 1 + 0.35 * np.sign(np.sin(2 * np.pi * 23 * tt))   # tironeo del motor
    return s
poner(servo(1.4, 180, 120), tf(50), 0.32, pan=0.75)
poner(clic(0.6), tf(78), 0.35, pan=0.75)
poner(servo(1.4, 170, 115), tf(72), 0.32, pan=-0.75)
poner(clic(0.6), tf(100), 0.35, pan=-0.75)
poner(servo(1.1, 150, 95), tf(130), 0.38, pan=0.0)

# 6) Pasos de Freddy por el comedor (audio real del juego) desde que empieza a caminar hasta que se detiene
pasos = leer(AUD + '/game_over/freddy_footsteps.wav')
seg = pasos[:int((tf(146) - tf(36)) * SR)]
seg = seg * np.linspace(0.45, 1.0, len(seg))     # se acerca
poner(seg, tf(36), 0.85, pan=-0.3, fade_in=0.2, fade_out=0.25)

# 7) Los focos fallan (frames 104-146): chasquidos electricos + zumbido de 60 Hz que se corta
n = int((tf(146) - tf(104)) * SR); tt = np.arange(n) / SR
zumbido = (np.sin(2 * np.pi * 60 * tt) + 0.5 * np.sin(2 * np.pi * 120 * tt) + 0.3 * np.sin(2 * np.pi * 180 * tt)) * 0.12
corte = np.ones(n)
for _ in range(14):
    a = rng.integers(0, n - 2000); corte[a:a + rng.integers(400, 3000)] = 0.05
poner(zumbido * corte, tf(104), 1.0)
for _ in range(9):
    poner(clic(0.9) * 0.7, rng.uniform(tf(104), tf(145)), 0.45, pan=rng.uniform(-0.6, 0.6))
poner(golpe() * 1.2, tf(146), 0.9)     # se apaga todo

# 8) Estatica final (audio real del juego), corte seco al terminar los fotogramas
est = leer(AUD + '/game_over/static.wav')[:int(0.55 * SR)]
poner(est, tf(160) + 0.05, 0.75)

# normalizacion con techo de -1 dBFS y escritura
pico = np.abs(mix).max(); mix *= (10 ** (-1 / 20)) / pico
datos = (np.clip(mix, -1, 1) * 32767).astype(np.int16)
w = wave.open(OUT, 'wb'); w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR); w.writeframes(datos.tobytes()); w.close()
print('escrito', OUT, round(len(datos) / SR, 2), 's, pico original', round(pico, 3))
