# Construye la escena de la cinematica "Rendirse" (FDAF) con los modelos reales de Escape y la guarda como .blend.
import bpy, mathutils, math, sys
OUT=sys.argv[sys.argv.index('--')+1]
M='C:/Users/dfarl/git/five_doors_escape/assets/models/'
bpy.ops.wm.read_factory_settings(use_empty=True)
def G(x,y,z): return mathutils.Vector((x,-z,y))   # coordenadas del juego (Y arriba) -> Blender (Z arriba)
def importar(nombre):
    antes=set(bpy.data.objects); bpy.ops.import_scene.gltf(filepath=M+nombre+'.glb')
    nuevos=[o for o in bpy.data.objects if o not in antes]
    for o in list(nuevos):
        if o.name.startswith('Icosphere'): bpy.data.objects.remove(o, do_unlink=True); nuevos.remove(o)
    return nuevos
mapa=importar('pizzeria')
for o in mapa: o['fdaf_mapa']=1
def personaje(nombre, escala, pos, yaw, accion):
    objs=importar(nombre)
    raiz=bpy.data.objects.new('ROOT_'+nombre,None); bpy.context.scene.collection.objects.link(raiz)
    for o in objs:
        if o.parent is None: o.parent=raiz
    raiz.scale=(escala,)*3; raiz.location=pos; raiz.rotation_euler=(0,0,math.radians(yaw))
    arm=[o for o in objs if o.type=='ARMATURE'][0]
    ac=bpy.data.actions.get(accion)
    if arm.animation_data is None: arm.animation_data_create()
    arm.animation_data.action=ac
    try: arm.animation_data.action_slot=ac.slots[0]
    except Exception as e: print('slot',e)
    print('PERSONAJE',nombre,'acciones disponibles',[a.name for a in bpy.data.actions if nombre[:3].lower() in a.name.lower()], 'asignada',ac and ac.name, 'rango',ac and tuple(ac.frame_range))
    return raiz, arm
sc=bpy.context.scene
chica=personaje('chica',0.02289,G(-1.45,0.56,-8.05),0,'Chica_Idle')
bonnie=personaje('bonnie',0.01545,G(1.45,0.56,-8.05),0,'Bonnie_Idle')
freddy=personaje('freddy',10.0,G(0.4,0.0,1.5),180,'Freddy_Walk')
bpy.ops.wm.save_as_mainfile(filepath=OUT)
