# Direccion completa de la cinematica "Rendirse" (FDAF): posiciones, animaciones, luces, camara panoramica y render.
# Uso: blender -b surrender_base.blend -P direct_surrender.py -- salida(con ####) frames(ej "1,60,160" o "1-160") muestras ancho
import bpy, mathutils, math, sys, random, os
a = sys.argv[sys.argv.index('--') + 1:]
OUT = a[0]; FR = a[1]; SPP = int(a[2]); W = int(a[3])


def G(x, y, z):
    """Coordenadas del juego (Y arriba, como en Escape) -> Blender (Z arriba)."""
    return mathutils.Vector((x, -z, y))


sc = bpy.context.scene
sc.render.fps = 20
random.seed(1987)
ob = bpy.data.objects

# ---------------- personajes
chica_root, bonnie_root, freddy_root = ob['ROOT_chica'], ob['ROOT_bonnie'], ob['ROOT_freddy']
arm = {n: [o for o in ob if o.type == 'ARMATURE' and o.parent == ob['ROOT_' + n]][0] for n in ('chica', 'bonnie', 'freddy')}
# Chica a la izquierda y Bonnie a la derecha del jugador (como en la referencia de FNAF 2), un poco adelantados
chica_root.location = G(1.25, 0.56, -7.35)
bonnie_root.location = G(-1.25, 0.56, -7.35)


def extremos_z(root):
    dg = bpy.context.evaluated_depsgraph_get(); zs = []
    for o in ob:
        p = o
        while p and p != root:
            p = p.parent
        if p == root and o.type == 'MESH':
            e = o.evaluated_get(dg)
            for v in e.data.vertices:
                zs.append((e.matrix_world @ v.co).z)
    return min(zs), max(zs)


def nla(armobj, accion, inicio, repeticiones=1):
    ad = armobj.animation_data or armobj.animation_data_create()
    act = bpy.data.actions[accion]
    tr = ad.nla_tracks.new()
    s = tr.strips.new(accion + str(inicio), int(inicio), act)
    try:
        s.action_slot = act.slots[0]
    except Exception:
        pass
    s.repeat = repeticiones
    s.extrapolation = 'HOLD'
    return s


for n in ('chica', 'bonnie', 'freddy'):
    arm[n].animation_data.action = None
nla(arm['chica'], 'Chica_Idle', 1, 2)
nla(arm['bonnie'], 'Bonnie_Idle', 1, 2)
ad_f = arm['freddy'].animation_data or arm['freddy'].animation_data_create()
pista_f = ad_f.nla_tracks.new()
for nombre, ini, rep in (('Freddy_Idle', 1, 1), ('Freddy_Walk', 36, 4.55), ('Freddy_Idle', 147, 1)):
    acc = bpy.data.actions[nombre]
    st = pista_f.strips.new(nombre + str(ini), ini, acc)
    try:
        st.action_slot = acc.slots[0]
    except Exception:
        pass
    st.repeat = rep
    if nombre == 'Freddy_Idle' and ini == 1:
        st.action_frame_end = min(st.action_frame_end, st.action_frame_start + 34)
    st.extrapolation = 'HOLD_FORWARD'
    print('tira', nombre, st.frame_start, st.frame_end)

sc.frame_set(1); bpy.context.view_layer.update()
z0, z1 = extremos_z(freddy_root)
print('freddy altura original', z1 - z0)
freddy_root.scale = freddy_root.scale * (2.05 / (z1 - z0))
bpy.context.view_layer.update()
z0, z1 = extremos_z(freddy_root)
print('freddy altura nueva', z1 - z0, 'pie', z0)

# Freddy espera en la oscuridad al fondo del pasillo izquierdo, avanza entre las mesas (frames 36-124) y sale al frente del escenario
PIE = mathutils.Vector((0, 0, z0))
def pose_freddy(f, x, z, yaw):
    freddy_root.location = G(x, 0, z) - PIE; freddy_root.keyframe_insert('location', frame=f)
    freddy_root.rotation_euler = (0, 0, math.radians(yaw)); freddy_root.keyframe_insert('rotation_euler', frame=f)
pose_freddy(1, -1.75, 1.6, 180)
pose_freddy(36, -1.75, 1.6, 180)
pose_freddy(124, -1.75, -5.7, 180)
pose_freddy(146, 0.0, -6.5, 180 - 55)
pose_freddy(152, 0.0, -6.5, 180)

# ---------------- giro de cabezas hacia el jugador (Damped Track sobre un objetivo en la camara)
OJO_CAM = G(0, 0.56 + 1.70, -8.05)
obj_cam = bpy.data.objects.new('mira_jugador', None); sc.collection.objects.link(obj_cam); obj_cam.location = OJO_CAM
EJES = os.environ.get('EJES', 'TRACK_Z,TRACK_Z,TRACK_Z').split(',')


def mirar(armobj, hueso, eje, f0, f1):
    pb = armobj.pose.bones[hueso]
    c = pb.constraints.new('DAMPED_TRACK'); c.target = obj_cam; c.track_axis = eje
    c.influence = 0; c.keyframe_insert('influence', frame=f0)
    c.influence = 0.85; c.keyframe_insert('influence', frame=f1)
    return c


mirar(arm['bonnie'], 'bip_head_bonnie.qc_skeleton', EJES[0], 50, 78)
mirar(arm['chica'], 'Bone.003_Armature_7', EJES[1], 72, 100)
mirar(arm['freddy'], 'j_head_04', EJES[2], 130, 152)

# ojos brillantes (puntos emisivos) pegados a los huesos de los ojos de Freddy -- se encienden al final, como en FNAF
mat = bpy.data.materials.new('ojo')
try:
    mat.use_nodes = True
except Exception:
    pass
for n in list(mat.node_tree.nodes):
    mat.node_tree.nodes.remove(n)
bsdf = mat.node_tree.nodes.new('ShaderNodeEmission')
salida_mat = mat.node_tree.nodes.new('ShaderNodeOutputMaterial')
mat.node_tree.links.new(bsdf.outputs[0], salida_mat.inputs[0])
bsdf.inputs['Color'].default_value = (1, 0.97, 0.9, 1)


def ojo(armobj, hueso, radio):
    bpy.ops.mesh.primitive_uv_sphere_add(radius=radio, segments=12, ring_count=8)
    s = bpy.context.object; s.data.materials.append(mat)
    mw = armobj.matrix_world @ armobj.pose.bones[hueso].matrix
    s.parent = armobj; s.parent_type = 'BONE'; s.parent_bone = hueso
    s.matrix_world = mw
    return s


for h in ('j_l_eye_08', 'j_r_eye_011'):
    ojo(arm['freddy'], h, 0.02)
bsdf.inputs['Strength'].default_value = 0; bsdf.inputs['Strength'].keyframe_insert('default_value', frame=120)
bsdf.inputs['Strength'].default_value = 60; bsdf.inputs['Strength'].keyframe_insert('default_value', frame=136)

# ---------------- render y camara panoramica (frente = eje -Z local; (90,0,180) mira hacia +Z del juego)
sc.render.engine = 'CYCLES'
prefs = bpy.context.preferences.addons['cycles'].preferences
for t in ('OPTIX', 'CUDA'):
    try:
        prefs.compute_device_type = t; prefs.refresh_devices()
        if any(d.type == t for d in prefs.devices):
            break
    except Exception:
        pass
for d in prefs.devices:
    d.use = d.type != 'CPU'
sc.cycles.device = 'GPU'; sc.cycles.samples = SPP; sc.cycles.use_denoising = True; sc.cycles.max_bounces = 4
sc.render.resolution_x = W; sc.render.resolution_y = W // 3; sc.render.resolution_percentage = 100
sc.view_settings.view_transform = 'AgX'; sc.view_settings.look = 'AgX - Punchy'
sc.render.image_settings.file_format = 'JPEG'; sc.render.image_settings.quality = 88
cam = bpy.data.cameras.new('cam'); co = bpy.data.objects.new('cam', cam); sc.collection.objects.link(co); sc.camera = co
cam.type = 'PANO'; cam.panorama_type = 'EQUIRECTANGULAR'
cam.longitude_min = -math.radians(80); cam.longitude_max = math.radians(80)
cam.latitude_min = -math.radians(26.7); cam.latitude_max = math.radians(26.7)
co.rotation_euler = (math.radians(86), 0, math.radians(180))
# respiracion: leve balanceo de la camara (el jugador esta dentro del traje)
for f in range(1, 171, 10):
    co.location = OJO_CAM + mathutils.Vector((0.006 * math.sin(f * 0.31), 0, 0.012 * math.sin(f * 0.21)))
    co.keyframe_insert('location', frame=f)

# ---------------- mundo y luces
w = bpy.data.worlds.new('w'); sc.world = w
nt = w.node_tree
bg = nt.nodes.get('Background') or nt.nodes.new('ShaderNodeBackground')
on = nt.nodes.get('World Output') or nt.nodes.new('ShaderNodeOutputWorld')
nt.links.new(bg.outputs[0], on.inputs[0]); bg.inputs[0].default_value = (0.004, 0.005, 0.009, 1)


def luz(nombre, tipo, pos, energia, color, objetivo=None, tam=None, spot=None):
    L = bpy.data.lights.new(nombre, tipo); L.energy = energia; L.color = color
    if tam is not None:
        if tipo == 'AREA':
            L.size = tam
        else:
            L.shadow_soft_size = tam
    if spot:
        L.spot_size = math.radians(spot[0]); L.spot_blend = spot[1]
    o = bpy.data.objects.new(nombre, L); sc.collection.objects.link(o); o.location = pos
    if objetivo is not None:
        o.rotation_euler = (objetivo - pos).to_track_quat('-Z', 'Y').to_euler()
    return L


LUNA = float(os.environ.get('LUNA', '260')); FOCO = float(os.environ.get('FOCO', '260'))
luna = luz('luna', 'AREA', G(0, 2.75, -1.0), LUNA, (0.50, 0.62, 1.0), objetivo=G(0, 0, -1.0), tam=7)
fc = luz('foco_chica', 'SPOT', G(2.2, 2.78, -4.6), FOCO, (1.0, 0.80, 0.58), objetivo=G(1.30, 1.7, -7.70), spot=(24, 0.45))
fb = luz('foco_bonnie', 'SPOT', G(-2.2, 2.78, -4.6), FOCO, (1.0, 0.80, 0.58), objetivo=G(-1.30, 1.7, -7.70), spot=(24, 0.45))
# luz fria y tenue sobre el propio escenario, detras del jugador: es la que deja ver las caras cuando giran hacia el
ESC = float(os.environ.get('ESC', '90'))
escenario = luz('luz_escenario', 'AREA', G(0, 2.82, -8.9), ESC, (0.62, 0.70, 1.0), objetivo=G(0, 1.2, -6.8), tam=3.2)
pasillo = luz('foco_comedor', 'SPOT', G(0, 2.78, -2.5), FOCO * 0.8, (0.85, 0.9, 1.0), objetivo=G(0, 0, -3.6), spot=(40, 0.6))


def k(L, f, v):
    L.energy = v; L.keyframe_insert('energy', frame=f)


# parpadeo: los focos fallan desde el frame 104 y se apagan del todo en 146 (solo quedan los ojos)
for L, base in ((fc, FOCO), (fb, FOCO), (pasillo, FOCO * 0.8)):
    k(L, 1, base); k(L, 104, base)
    f = 104
    while f < 146:
        k(L, f, base * random.choice((0.05, 0.2, 1.0, 0.6))); f += random.randint(1, 3)
    k(L, 146, 0)
k(luna, 1, LUNA); k(luna, 140, LUNA); k(luna, 150, LUNA * 0.15)
k(escenario, 1, ESC); k(escenario, 138, ESC); k(escenario, 146, ESC * 0.08)

# ---------------- render
if os.environ.get('DEBUGCAM'):
    dc = bpy.data.cameras.new('dbg'); do = bpy.data.objects.new('dbg', dc); sc.collection.objects.link(do); sc.camera = do
    dc.lens = 24; do.location = OJO_CAM
    objetivo_dbg = {'B': G(-1.25, 2.2, -7.35), 'C': G(1.25, 2.2, -7.35), 'F': G(0, 1.9, -6.5)}.get(os.environ['DEBUGCAM'], G(0, 2.0, -5.0))
    if os.environ['DEBUGCAM'] == '1':
        do.location = G(0, 2.0, -5.0); objetivo_dbg = G(0, 2.0, -7.6)
    do.rotation_euler = (objetivo_dbg - do.location).to_track_quat('-Z', 'Y').to_euler()
    sc.render.resolution_y = W * 9 // 16
fr = []
for p in FR.split(','):
    if '-' in p:
        i, j = p.split('-'); fr += list(range(int(i), int(j) + 1))
    else:
        fr.append(int(p))
for f in fr:
    sc.frame_set(f)
    sc.render.filepath = OUT.replace('####', '%04d' % f)
    bpy.ops.render.render(write_still=True)
