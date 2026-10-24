"""Sintetiza audio/sfx/nyder_star_sparkle.wav: el destello de la estrella del logo NYDER GAMES (CLAUDE.md §1.65).

Sonido original, generado por completo aquí (sin muestras externas): un arpegio ascendente muy rápido de
campanillas (parciales inarmónicos con decaimiento exponencial y un leve vibrato que "brilla"), un soplo de aire
filtrado hacia los agudos que acompaña la subida de luz y una cola corta. Suave, sin golpe inicial: no debe sonar
como un clic ni como un susto. Pico a -9 dBFS, 44.1 kHz, estéreo (las campanillas se abren un poco a los lados).

Uso: python tools/audio/synth_nyder_sparkle.py [salida.wav]
"""
import sys
import wave

import numpy as np

SR = 44100
DUR = 1.6
rng = np.random.default_rng(1987)
t = np.arange(int(SR * DUR)) / SR
izq = np.zeros_like(t)
der = np.zeros_like(t)


def campanilla(f0, inicio, amp, pan, decay=2.6):
    env = np.zeros_like(t)
    m = t >= inicio
    tt = t[m] - inicio
    # ataque de 6 ms (sin clic) y caída exponencial
    env[m] = np.minimum(1.0, tt / 0.006) * np.exp(-decay * tt)
    vib = 1.0 + 0.0025 * np.sin(2 * np.pi * 6.5 * t)
    s = np.zeros_like(t)
    # parciales de campana (inarmónicos) con su propio decaimiento
    for ratio, a, d in ((1.0, 1.0, 1.0), (2.76, 0.35, 1.8), (5.40, 0.16, 2.6), (8.93, 0.07, 3.4)):
        if f0 * ratio > 16000:   # nada cerca de Nyquist (evita aliasing y estridencia)
            continue
        s += a * np.sin(2 * np.pi * f0 * ratio * vib * t) * np.exp(-decay * (d - 1) * np.maximum(t - inicio, 0))
    s *= env * amp
    return s * (1 - pan) / 1.0, s * (1 + pan) / 1.0


# arpeggio ascendente (Do6 - Mi6 - Sol6 - Do7 - Mi7) en ~0.33 s, centrado en el pico de luz
notas = [(1046.5, 0.00, -0.35), (1318.5, 0.07, 0.30), (1568.0, 0.14, -0.20), (2093.0, 0.22, 0.25), (2637.0, 0.31, 0.0)]
for i, (f, ini, pan) in enumerate(notas):
    a = 0.22 + 0.05 * i
    l, r = campanilla(f, 0.12 + ini, a, pan, decay=2.4 + 0.3 * i)
    izq += l
    der += r

# soplo de aire: ruido filtrado (pasa altos por diferencias) con envolvente que sube y baja con la luz
ruido = rng.standard_normal(t.size)
for _ in range(3):
    ruido = np.diff(ruido, prepend=0.0)
ruido /= np.max(np.abs(ruido))
env_aire = np.exp(-((t - 0.38) / 0.22) ** 2)
izq += 0.05 * ruido * env_aire
der += 0.05 * np.roll(ruido, 37) * env_aire

# cola: reverberación simple (ecos atenuados)
for retardo, g in ((0.043, 0.35), (0.071, 0.25), (0.113, 0.18), (0.167, 0.12)):
    n = int(retardo * SR)
    izq[n:] += g * der[:-n]
    der[n:] += g * izq[:-n]

# fundido final y normalización a -9 dBFS
fade = np.ones_like(t)
fade[-int(0.25 * SR):] = np.linspace(1, 0, int(0.25 * SR))
izq *= fade
der *= fade
pico = max(np.max(np.abs(izq)), np.max(np.abs(der)))
objetivo = 10 ** (-9 / 20)
izq *= objetivo / pico
der *= objetivo / pico

salida = sys.argv[1] if len(sys.argv) > 1 else 'FiveDoorsAtFreddys/src/com/fdaf/resources/audio/sfx/nyder_star_sparkle.wav'
datos = (np.stack([izq, der], axis=1) * 32767).astype('<i2')
with wave.open(salida, 'wb') as w:
    w.setnchannels(2)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(datos.tobytes())
print('escrito', salida, len(t) / SR, 's')
