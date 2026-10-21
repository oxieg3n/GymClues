"""Build offline Android muscle-map images and their explicitly assigned exercise metadata.

Run: python tools/build_muscle_maps.py
No network, image API, or third-party Python packages are required. Unknown program
exercises fail generation instead of silently inheriting a guessed muscle map.
"""
import json
import re
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main"
PROFILES = {}
ASSIGNMENTS = {}


def key(name):
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")


def profile(id, primary, assisting, stabilizers, names, note="", kind="strength", source=""):
    PROFILES[id] = dict(primary=primary.split(), assisting=assisting.split(),
                        stabilizers=stabilizers.split(), note=note, kind=kind,
                        source=source or "https://www.nasm.org/resource-center/exercise-library")
    for name in names.split("|"):
        if name.strip():
            k = key(name.strip())
            assert k not in ASSIGNMENTS or ASSIGNMENTS[k][0]["profile"] == id, f"Conflicting assignment: {name}"
            ASSIGNMENTS[k] = [dict(label="", profile=id)]


# Role assignments describe typical controlled technique, not measured activation.
# Secondary movers are kept separate from muscles primarily holding a joint still.
profile("bench", "pecs", "front_delts triceps", "rotator_cuff serratus traps_mid", """
Barbell Bench Press|Bench press|Flat barbell bench press|Flat barbell bench press - back-off|
Dumbbell Bench|Dumbbell Bench Press|Dumbbell Floor Press|Floor Dumbbell Press|Reverse Grip Dumbbell Press
""", "Flat or floor press. The floor limits shoulder extension; dumbbells require more independent shoulder control.",
source="https://www.nasm.org/resource-center/exercise-library/barbell-bench-press")
profile("incline", "upper_pecs", "pecs front_delts triceps", "rotator_cuff serratus traps_mid", """
Barbell Incline Bench Press|Incline Barbell Bench Press|Incline bench press|Incline bench press (dumbbell or barbell)|
Incline Dumbbell Bench Press|Incline Dumbbell Press|Incline press
""", "An incline biases the clavicular (upper) chest. Shoulder contribution increases as the bench gets steeper.")
profile("decline", "pecs", "triceps front_delts", "rotator_cuff traps_mid", "Decline bench press|Decline Floor Press",
        "Lower chest emphasis; the whole pectoralis major still contributes.")
profile("machine_press", "pecs", "front_delts triceps", "rotator_cuff", "Machine bench press|Machine Chest Press|Smith flat bench press|Smith bench press (no added weight)",
        "The machine or Smith track guides the load, reducing balance demands compared with free weights.")
profile("machine_incline", "upper_pecs", "pecs front_delts triceps", "rotator_cuff", "Machine Incline Chest Press",
        "Inclined, supported machine press; upper chest emphasis.")
profile("close_press", "triceps", "pecs front_delts", "rotator_cuff serratus traps_mid", "Close Grip Bench Press|Close-grip bench press|Close Grip Dumbbell Press|Barbell JM Press",
        "Triceps emphasis. A JM press blends pressing and elbow extension; its chest contribution is smaller.")
profile("pushup", "pecs", "front_delts triceps serratus", "abs deep_core obliques glutes rotator_cuff", "Push Up|Push-up",
        "The trunk stays braced as the arms press. Serratus anterior moves and controls the shoulder blades.")
profile("close_pushup", "triceps", "pecs front_delts serratus", "abs deep_core obliques glutes rotator_cuff", "Close Grip Push Up")
profile("dip", "triceps pecs", "front_delts", "rotator_cuff serratus traps_lower abs", "Assisted bar dip|Bar dip|Bodyweight dip|Dip|Triceps dip|Weighted or bodyweight triceps dip",
        "More forward torso lean generally increases chest contribution. Assistance reduces the load, not the basic muscle pattern.")
profile("machine_dip", "triceps", "pecs front_delts", "rotator_cuff", "Machine Tricep Dip", "Supported seated dip machine.")
profile("fly", "pecs", "front_delts", "rotator_cuff biceps", "Dumbbell Chest Fly|Dumbbell Flys|Flat Bench Dumbbell Flye|Floor Dumbbell Fly",
        "Biceps help hold the elbow angle; they are not the main mover. A floor fly limits the bottom range.")
profile("cable_fly", "pecs", "front_delts", "rotator_cuff abs deep_core obliques", "Low Cable Fly Crossovers|Standing cable crossover|Costal pec press-around",
        "Standing cable fly or press-around. Torso bracing resists the cable's pull; cable height changes chest emphasis.")
profile("incline_fly", "upper_pecs", "pecs front_delts", "rotator_cuff biceps", "Cable Incline Fly")
profile("pec_deck", "pecs", "front_delts", "rotator_cuff", "Machine fly|Machine Pec Deck|Pec Dec|Pec deck fly|Seated machine fly",
        "Chest fly with the torso supported. Handle and elbow-pad designs have different stabilization demands.")
profile("pullover", "lats pecs", "triceps serratus", "rotator_cuff abs deep_core", "Dumbbell Pullover|Dumbbell Pullovers",
        "Shoulder extension involves both lats and chest. Rib position, elbow angle and range affect emphasis.")
profile("row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "erectors abs deep_core forearm_flexors rotator_cuff", """
Barbell Bent Over Row|Barbell row|Bent Over Row|Bent-Over Barbell Row|Bent Over Dumbbell Row|T-Bar Row
""", "Unsupported bent-over row: spinal extensors and abdominal bracing hold the torso still.")
profile("one_arm_row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "obliques deep_core erectors forearm_flexors rotator_cuff", """
Dumbbell row|One Arm Dumbbell Row|One Arm Row|One-arm dumbbell row|One-arm row|One-arm row (dumbbell or barbell)|Single-Arm Dumbbell Row
""", "Single-arm row. Obliques resist torso rotation; hand or bench support reduces lower-back demand.")
profile("supported_row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "rotator_cuff forearm_flexors", """
Chest Supported Dumbbell Row|Chest Supported Row|Chest-supported row|Dumbbell Incline Bench Row|
Hammer Strength machine row|Leverage Machine Iso Row|T Bar Prone Row
""", "Chest support reduces the need to hold a bent-over torso position.",
source="https://www.nasm.org/resource-center/exercise-library/seated-machine-row-close-grip")
profile("seated_row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "erectors abs deep_core forearm_flexors rotator_cuff", """
Cable Seated Row|Overhand cable row|Seated cable row|Seated row
""", "Seated row without chest support. Keep the torso steady instead of rocking with the load.")
profile("one_arm_cable_row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "obliques deep_core erectors rotator_cuff forearm_flexors", "One-arm cable row",
        "Single-arm cable row: the trunk resists the cable's rotational pull.")
profile("high_row", "lats traps_mid rhomboids rear_delts", "biceps brachialis", "rotator_cuff forearm_flexors", "High Row",
        "Assumes a supported high-row machine. Elbow path changes the balance between lats and upper back.")
profile("inverted_row", "lats traps_mid rhomboids", "rear_delts biceps brachialis", "abs deep_core glutes rotator_cuff forearm_flexors", "Inverted row",
        "Bodyweight row; core and glutes hold the body in a straight line.")
profile("pulldown", "lats", "biceps brachialis traps_lower rhomboids rear_delts", "rotator_cuff abs deep_core forearm_flexors", """
Cable Lat Pulldown|Cable Lat Pulldown (Wide Grip)|Lat Pull Down|Lat pulldown|Lat pulldown (neutral grip)|
Lat pulldown (pronated grip)|Machine Lat Pulldown|Pulldown|V-Bar Lat Pull Down
""", "Vertical pull. Grip and elbow path change emphasis; forearm flexors maintain the grip.")
profile("one_arm_pulldown", "lats", "biceps brachialis traps_lower", "rotator_cuff obliques deep_core forearm_flexors", "Cable One-Arm Lat Pulldown",
        "Single-arm pulldown; obliques help resist leaning and rotation.")
profile("pullup", "lats", "biceps brachialis traps_lower rhomboids rear_delts", "abs deep_core rotator_cuff forearm_flexors", """
Pull Up|Pullup|Pull-up|Pull-up (self or assisted)|Weighted or bodyweight pull-up|Wide Grip Pull-Ups
""", "Overhand pull-up. Assisted versions reduce load while preserving the basic pulling pattern.",
source="https://www.nasm.org/resource-center/exercise-library/pull-up")
profile("chinup", "lats biceps", "brachialis traps_lower rhomboids", "abs deep_core rotator_cuff forearm_flexors", "Chin Up|Chin-up|Chin-Ups|Weighted or bodyweight chin-up",
        "Underhand grip gives the biceps a stronger role than in a typical overhand pull-up.")
profile("straight_arm_pull", "lats", "pecs triceps rear_delts", "rotator_cuff abs deep_core obliques", "Cable Straight Arm Lat Pulldown|Straight Arm Pressdown|Straight-arm cable pulldown",
        "Shoulder extension with a nearly fixed elbow angle. Triceps assist shoulder extension and hold the elbow position.")
profile("overhead_press", "front_delts side_delts", "triceps traps_upper serratus", "rotator_cuff abs deep_core obliques glutes", """
Barbell Clean and Press|Military Press|Overhead Press|Shoulder Press|Standing overhead press|Standing overhead press - back-off
""", "Standing strict press. The clean-and-press has an additional lower-body pull phase; see its separate option below.")
profile("seated_press", "front_delts side_delts", "triceps traps_upper serratus", "rotator_cuff abs deep_core", """
Arnold Press|Dumbbell Military Press|Dumbbell Seated Shoulder Press|Dumbbell Shoulder Press|Seated Barbell Overhead Press|
Seated behind-the-neck press|Seated Dumbbell Press|Seated Overhead Dumbbell Press
""", "Seated press; back support reduces torso demands. Arnold presses add rotation. Behind-the-neck variations require suitable shoulder mobility.")
profile("machine_shoulder_press", "front_delts side_delts", "triceps traps_upper serratus", "rotator_cuff", "Machine Seated Shoulder Press|Machine Shoulder Press|Smith Machine Shoulder Press|Smith overhead press",
        "Guided overhead press. A supported torso and guided path reduce balance demands.")
profile("lateral_raise", "side_delts", "traps_upper serratus", "rotator_cuff abs deep_core", """
Cable Deltoid Raise|Cable Lateral Raise|Cable lateral raise (lengthened position)|Cable One-Arm Lateral Raise|
Dumbbell lateral raise|Dumbbell Side Lateral Raise|Lateral raise|Lateral raise (superset 1)|Lateral raise (superset 2)|
Lateral Raises|Side Lateral Raise|Standing cable lateral raise
""", "Raise out to the side. The supraspinatus (part of the rotator cuff) also assists the lift while stabilizing the shoulder.")
profile("seated_raise", "side_delts", "traps_upper serratus", "rotator_cuff", "Seated dumbbell lateral raise|Seated Lateral Raise",
        "Seated lateral raise; sitting reduces whole-body balance demands.")
profile("fdl_raise", "front_delts side_delts rear_delts", "traps_upper serratus", "rotator_cuff", "Seated FDL dumbbell raise",
        "FDL is interpreted as a front / lateral / rear-delt raise sequence. Each phase emphasizes a different deltoid head.")
profile("rear_fly", "rear_delts", "traps_mid rhomboids", "rotator_cuff erectors abs deep_core", """
Bent-Over Rear Lateral Raise|Dumbbell Rear Delt Fly|Rear Delt Reverse Fly|Rear-delt fly|Cable Rear Delt Fly|Standing cable reverse fly
""", "Rear-delt fly. Bent-over versions need spinal support; standing cable versions need torso bracing.")
profile("reverse_deck", "rear_delts", "traps_mid rhomboids", "rotator_cuff", "Rear-delt machine fly|Reverse machine fly|Reverse Pec Dec|Seated machine reverse fly",
        "Supported reverse fly; shoulder blades retract as the arms open.")
profile("face_pull", "rear_delts rotator_cuff", "traps_mid traps_lower rhomboids", "abs deep_core obliques", "Cable Face Pulls|Cable Rope Face Pull|Face pull",
        "Assumes a face pull ending with external shoulder rotation. External rotators both move and stabilize the shoulder.")
profile("pull_apart", "rear_delts traps_mid rhomboids", "rotator_cuff", "abs deep_core", "Banded pull-apart|Resistance-band pull-apart",
        "Warm-up activation, rather than a heavy working set.", kind="activation")
profile("external_rotation", "rotator_cuff", "rear_delts", "traps_mid rhomboids abs", "Shoulder external rotation",
        "Infraspinatus and teres minor externally rotate the shoulder. The cuff is shown schematically.", kind="activation")
profile("internal_rotation", "rotator_cuff", "pecs lats", "traps_mid rhomboids abs", "Shoulder internal rotation",
        "Subscapularis is the principal cuff internal rotator; assistance depends on arm position.", kind="activation")
profile("upright_row", "side_delts traps_upper", "biceps brachialis front_delts", "rotator_cuff abs deep_core forearm_flexors", "Barbell Upright Row|Cable Upright Row|One Arm Upright Row|Upright Row")
profile("shrug", "traps_upper", "", "rotator_cuff erectors abs deep_core forearm_flexors", """
Barbell shrug|Dumbbell Shoulder Shrug|Dumbbell Shrug|Dumbbell Shrugs|Shoulder shrug|Shrug|Shrugs|
Shrug (dumbbell, barbell or trap bar)|Single Arm Dumbbell Shrug
""", "Upper trapezius elevates the shoulder blades. Levator scapulae also assists but is not separately drawn.")
profile("seated_shrug", "traps_upper", "", "rotator_cuff forearm_flexors", "Seated Dumbbell Shrug", "Seated shrug; the seat reduces postural demands.")
profile("curl", "biceps", "brachialis", "forearm_flexors rotator_cuff abs deep_core", """
Alternating biceps curl|Alternating Dumbbell Curls|Barbell Bicep Curl|Barbell biceps curl|Barbell curl|Biceps curl|
Cable Bicep Curl|Cable Curls|Dumbbell Bicep Curl|Dumbbell curl|Dumbbell Curls|EZ Bar Curl|EZ Bar Curls|EZ-bar curl|
Standing Barbell Curl|Standing Dumbbell Curl
""", "Supinated elbow curl. Forearm muscles stabilize the wrist; the torso stays still.")
profile("supported_curl", "biceps", "brachialis", "forearm_flexors rotator_cuff", """
Concentration Curls|Dumbbell Seated Bicep Curl|Dumbbell Spider Curl|Incline Curl|Incline dumbbell curl|
Machine Bicep Curl|Machine Preacher Curl|Preacher Curl|Seated Dumbbell Curl|Seated Incline Dumbbell Curl|Spider curl
""", "Seated, incline or upper-arm-supported curl. Shoulder position changes the biceps' working length.")
profile("hammer_curl", "brachialis brachioradialis", "biceps", "forearm_flexors forearm_extensors rotator_cuff abs deep_core", "Cable hammer curl|Cable Rope Hammer Curl|Dumbbell Hammer Curl|Dumbbell Hammer Curls|Hammer curl",
        "Neutral grip emphasizes brachialis and brachioradialis. Forearm flexors and extensors hold the wrist position.")
profile("zottman", "biceps brachialis brachioradialis", "", "forearm_flexors forearm_extensors rotator_cuff abs", "Zottman Curl",
        "Supinated lifting phase and pronated lowering phase involve the biceps, brachialis and brachioradialis.")
profile("wrist_curl", "forearm_flexors", "", "forearm_extensors", "Wrist Curls", "Forearm supported; wrist flexion targets the wrist and finger flexor group.")
profile("reverse_wrist", "forearm_extensors", "", "forearm_flexors", "Reverse Wrist Curls", "Forearm supported; wrist extension targets the forearm extensor group.")
profile("pushdown", "triceps", "", "rotator_cuff forearm_flexors abs deep_core", """
Cable One-Arm Tricep Pushdown (Reverse Grip)|Cable Tricep Extension|Cable Tricep Extensions|Cable Tricep Pushdown (V-Bar)|
Single-Arm Cable Triceps Extension|Standing triceps pushdown|Tricep Pushdown|Triceps Pressdown|Triceps pushdown|Triceps Rope Pushdown|Rope Extension
""", "Cable elbow extension with the upper arms held still. Rope Extension is interpreted as a rope pushdown.")
profile("overhead_extension", "triceps", "", "rotator_cuff serratus forearm_flexors abs deep_core", """
Cable overhead triceps extension|Cable Rope Overhead Tricep Extension|Dumbbell Tricep Extension|Dumbbell Triceps Extension|
EZ Bar Seated Reverse Grip French Press|One Arm Overhead Extension|Overhead dumbbell extension|Overhead Extension|
Overhead triceps extension|Seated Dumbbell Extension|Seated triceps extension (DB, rope or EZ-bar)|Standing Barbell Tricep Extension
""", "Overhead elbow extension lengthens the triceps long head. Seated or supported versions reduce torso demand.")
profile("skullcrusher", "triceps", "", "rotator_cuff forearm_flexors", "EZ-bar skullcrusher|Lying Dumbbell Extension|Skull Crushers|Skullcrusher|Skullcrushers",
        "Lying elbow extension; shoulder muscles hold the upper arms in position.")
profile("machine_triceps", "triceps", "", "rotator_cuff forearm_flexors", "Machine Tricep Extension",
        "Supported elbow-extension machine; upper-arm support reduces shoulder and torso demands.")
profile("kickback", "triceps", "", "rear_delts rotator_cuff erectors abs deep_core", "One Arm Kickback|Triceps Kickbacks",
        "Assumes a bent-over dumbbell kickback. Bench support reduces spinal stabilization demand.")
profile("squat", "quads glutes", "adductors", "erectors abs deep_core obliques glute_med calves", """
Back Squat|Barbell back squat|Barbell back squat - back-off|Barbell Squat|Dumbbell box squat|Dumbbell Plie Squat|
Dumbbell Squat|Goblet Squat|High-bar squat|Squat|Squats
""", "Knee and hip extension. A plie stance increases adductor involvement; depth and stance alter emphasis.")
profile("front_squat", "quads glutes", "adductors", "erectors abs deep_core obliques traps_upper calves", "Barbell front squat|Barbell front squat - back-off|Front Squat",
        "Anterior loading increases the demand to keep the torso upright.")
profile("machine_squat", "quads glutes", "adductors", "abs deep_core calves", "Hack Squat|Pendulum Squat|Smith Machine Squat",
        "Guided squat. Hack and pendulum pads support the torso; a Smith squat still requires active trunk bracing.")
profile("sissy_squat", "quads", "", "abs deep_core glutes calves", "Sissy Squat",
        "Knee-dominant squat with extended hips. Hand support reduces balance requirements.")
profile("leg_press", "quads glutes", "adductors", "abs deep_core calves", "Leg press|Machine Leg Press",
        "The back pad supports the torso. Foot placement and depth affect hip and knee contributions.")
profile("lunge", "quads glutes", "adductors", "glute_med abs deep_core obliques erectors calves", """
Barbell Lunge|Bulgarian Split Squat|Dumbbell Lunge|Dumbbell Rear Lunge|Dumbbell Split Squat|Lunge|Split Squat|Walking Lunge
""", "Split-stance or stepping leg exercise. Hip abductors help keep the pelvis level; both sides are shown for alternating sets.")
profile("lateral_lunge", "quads glutes adductors", "", "glute_med abs deep_core obliques calves", "Dumbbell Lateral Lunge",
        "Sideways lunge; adductors lengthen and contribute to returning to the starting position.")
profile("smith_lunge", "quads glutes", "adductors", "glute_med abs deep_core calves", "Smith reverse lunge", "Guided reverse lunge reduces free-weight balance demands.")
profile("stepup", "quads glutes", "adductors", "glute_med abs deep_core obliques calves", "Dumbbell Step Up|Dumbbell step-up",
        "Step-up on the working leg. Hip abductors stabilize the pelvis.")
profile("deadlift", "glutes quads", "hamstrings adductors", "erectors abs deep_core obliques lats traps_upper forearm_flexors", """
Barbell conventional deadlift|Barbell conventional deadlift - back-off|Deadlift|Deadlift (optional)|Dumbbell Deadlift|
Barbell snatch-grip deadlift|Barbell snatch-grip deadlift - back-off
""", "Conventional floor pull. Spinal extensors brace rather than deliberately flexing and extending the back. Snatch grip increases upper-back demand.",
source="https://www.nasm.org/resource-center/exercise-library/barbell-deadlift")
profile("rack_pull", "glutes", "hamstrings quads", "erectors abs deep_core lats traps_upper forearm_flexors", "Rack Deadlifts",
        "Raised starting position reduces range and usually reduces knee-extension demand relative to a floor deadlift.")
profile("rdl", "hamstrings glutes", "adductors", "erectors abs deep_core obliques lats forearm_flexors", """
Barbell Romanian Deadlift|Dumbbell Stiff Leg Deadlift|Dumbbell Stiff Leg Deadlifts|Dumbbell Stiff Legged Deadlift|
Romanian deadlift|Stiff Leg Deadlift|Stiff Leg Deadlifts|Stiff-Leg Deadlift
""", "Hip hinge with relatively little knee movement. Spinal extensors hold the back position.",
source="https://www.nasm.org/resource-center/exercise-library/dumbbell-romanian-deadlift")
profile("single_rdl", "hamstrings glutes", "adductors", "glute_med erectors abs deep_core obliques forearm_flexors calves", "Single Stiff-Leg Deadlift",
        "Single-leg hinge adds pelvic and balance control; both sides are shown for alternating sets.")
profile("good_morning", "hamstrings glutes", "adductors", "erectors abs deep_core obliques traps_upper", "Barbell good morning|Good Morning|Good Mornings",
        "Hip hinge with the load supported at the shoulders.")
profile("hip_thrust", "glutes", "hamstrings adductors", "abs deep_core obliques glute_med erectors", "Barbell hip thrust|Dumbbell Hip Thrust",
        "Hip extension with the upper back supported. Keep the trunk and pelvis controlled.")
profile("pullthrough", "glutes hamstrings", "adductors", "erectors abs deep_core obliques", "Cable Pull Through with Rope", "Cable-resisted hip hinge.")
profile("back_extension", "erectors glutes hamstrings", "", "abs deep_core glute_med", "Back Extension|Bodyweight low-back extension|Hyperextension",
        "A hip-dominant extension emphasizes glutes and hamstrings; controlled spinal extension increases erector involvement. This map shows both common techniques.")
profile("reverse_hyper", "glutes hamstrings", "erectors", "abs deep_core", "Reverse hyperextension", "Legs move while the torso is supported.")
profile("superman", "erectors glutes", "hamstrings rear_delts traps_lower", "abs deep_core", "Superman",
        "Prone trunk and hip extension with arms raised; keep the range controlled.")
profile("leg_curl", "hamstrings", "calves", "abs deep_core", """
Dumbbell Hamstring Curl|Hamstring curl|Leg Curl|Leg curl (seated or lying)|Leg Curls|Lying Leg Curl|
Machine Leg Curl (Prone)|Machine Lying Leg Curl|Machine Seated Leg Curl|Seated leg curl
""", "Knee flexion. Gastrocnemius can assist; seated and lying variants change hamstring length.")
profile("standing_leg_curl", "hamstrings", "calves", "glute_med glutes abs deep_core", "Standing unilateral leg curl",
        "Single-leg knee flexion with the torso upright. The stance hip and core stabilize the pelvis.")
profile("glute_ham", "hamstrings", "glutes calves", "erectors abs deep_core", "Glute Ham Raise", "Knee flexion with hip extension and trunk control.")
profile("leg_extension", "quads", "", "abs deep_core", "Leg extension|Leg Extensions|Machine Leg Extension|Seated leg extension", "Supported knee extension isolates the quadriceps.")
profile("abduction", "glute_med", "glutes", "abs deep_core", "Hip Abduction Machine|Machine Hip Abduction",
        "Gluteus medius and minimus abduct the hip; tensor fasciae latae also assists but is not separately drawn.")
profile("adduction", "adductors", "", "abs deep_core", "Hip Adduction Machine", "The inner-thigh adductor group draws the thighs together.")
profile("standing_calf", "calves soleus", "", "tibialis glute_med abs deep_core", """
Calf Exercise|Calf raise|Dumbbell Standing Calf Raise|Standing Calf Raise|Standing Dumbbell Calf Raise
""", "Straight-knee calf raise emphasizes gastrocnemius with soleus contributing. Unspecified calf exercises assume a standing, straight-knee version.")
profile("supported_calf", "calves soleus", "", "tibialis abs deep_core", "Calf Press|Calf Press On Leg Press|Leg Press Calf Raise|Machine calf raise|Smith machine calf raise|Smith Standing Calf Raise",
        "Straight-knee calf press or guided raise. The equipment reduces balance demands compared with an unsupported standing raise.")
profile("seated_calf", "soleus", "calves", "", "Seated Calf Raise|Seated Dumbbell Calf Raise",
        "Bent knees shorten the gastrocnemius, shifting emphasis toward soleus.")
profile("crunch", "abs", "obliques", "deep_core", "Cable Crunch|Crunch|Crunches|Machine crunch|Weighted Crunch",
        "Trunk flexion. Cable and machine versions add resistance; keep the hips relatively still.")
profile("situp", "abs hip_flexors", "obliques", "deep_core", "Decline sit-up", "A full sit-up involves both trunk flexion and hip flexion.")
profile("leg_raise", "hip_flexors", "", "abs obliques deep_core", "Lying Leg Raise",
        "Hip flexors raise the thighs. Abs resist the tendency for the lower back to arch; adding a pelvic curl gives the abs a moving role.")
profile("reverse_crunch", "abs", "obliques", "deep_core hip_flexors", "Reverse Crunch",
        "Abs curl the pelvis toward the ribs. Hip flexors help hold the legs in position rather than swinging them.")
profile("v_up", "abs hip_flexors", "obliques", "deep_core", "V-Ups", "Combined trunk and hip flexion brings the torso and legs together.")
profile("hanging_raise", "hip_flexors abs", "obliques", "deep_core lats rotator_cuff forearm_flexors", "Hanging leg raise|Hanging Leg Raises",
        "Abs posteriorly tilt the pelvis; hip flexors raise the thighs. Grip and shoulder muscles support the hang.")
profile("bicycle", "abs obliques", "hip_flexors", "deep_core", "Bicycle Crunches", "Trunk flexion and rotation with alternating hip flexion.")
profile("plank", "abs deep_core", "obliques", "serratus rotator_cuff glutes quads erectors", "Plank",
        "The target muscles work isometrically to resist spinal extension; targets need not move a joint.",
source="https://www.nasm.org/resource-center/exercise-library/plank")
profile("side_plank", "obliques deep_core", "glute_med", "rotator_cuff serratus glutes erectors", "Side Plank",
        "Lateral core stability. Quadratus lumborum also contributes but is not separately drawn; both sides are shown for alternating holds.")
profile("hollow", "abs deep_core", "obliques hip_flexors", "quads", "Hollow Hold", "Isometric anti-extension hold; maintain a controlled pelvic position.")
profile("rollout", "abs deep_core", "obliques lats pecs", "rotator_cuff serratus glutes triceps", "Barbell rollout", "Anti-extension core work as the arms reach overhead.")
profile("jump_squat", "quads glutes calves", "hamstrings soleus", "glute_med abs deep_core erectors", "Bodyweight Squat Jumps", "Explosive squat and jump; the same muscle groups control the landing.")
profile("deep_squat", "quads glutes adductors", "", "abs deep_core erectors calves", "Bodyweight deep squat", "Warm-up squat through a comfortable range.", kind="activation")
profile("pass_through", "pecs lats front_delts", "", "rotator_cuff traps_mid abs", "Banded shoulder pass-through",
        "Shoulder mobility: highlighted target regions are moved or lengthened, not necessarily strengthened. Use a wide, comfortable grip.", kind="mobility")
profile("child_cobra", "lats erectors abs hip_flexors", "", "rotator_cuff serratus", "Child pose to cobra",
        "Alternating positions lengthen different trunk regions. These are mobility targets, not simultaneous prime movers.", kind="mobility")
profile("child_reach", "lats erectors obliques", "", "rotator_cuff", "Child pose with reach", "Reach forward or sideways to lengthen the back and side of the trunk.", kind="mobility")
profile("leg_swing", "hip_flexors hamstrings glutes adductors", "", "glute_med abs deep_core calves", "Leg swing",
        "Dynamic hip mobility. Front-to-back and side-to-side swings move different regions; the stance leg stabilizes the pelvis.", kind="mobility")
profile("lunge_arch", "hip_flexors abs", "", "glutes glute_med deep_core", "Low lunge arch", "Mobility targets the front of the hip and trunk; avoid forcing lumbar extension.", kind="mobility")
profile("side_lunge_stretch", "adductors", "", "quads glutes glute_med deep_core", "Side lunge stretch", "Inner-thigh stretch; the bent leg supports the position.", kind="mobility")
profile("hamstring_stretch", "hamstrings", "", "glute_med abs deep_core calves", "Standing hamstring stretch", "Lengthen the hamstrings with a controlled hip hinge; hand support reduces balance demand.", kind="mobility")
profile("world_stretch", "hip_flexors adductors hamstrings glutes pecs", "", "obliques deep_core rotator_cuff serratus", "World’s greatest stretch|World's greatest stretch",
        "Multi-position lunge and rotation sequence. Highlights show regions mobilized across the sequence, rather than simultaneous contraction.", kind="mobility")
profile("foam_roll", "lats traps_mid rear_delts pecs", "", "", "Upper-body foam rolling (optional)",
        "Shown regions are possible soft-tissue targets, not working muscles. The program does not prescribe a specific rolling area or position.", kind="recovery")

# Explicit choices for prescriptions that offer materially different movements.
def choices(name, variants):
    ASSIGNMENTS[key(name)] = [dict(label=label, profile=id) for label, id in variants]

choices("Pull Ups or Machine Rows", [("Pull-up", "pullup"), ("Machine row", "supported_row")])
choices("Pulldown or pull-up or chin-up", [("Pulldown", "pulldown"), ("Pull-up", "pullup"), ("Chin-up", "chinup")])
choices("Pull-up or pulldown", [("Pull-up", "pullup"), ("Pulldown", "pulldown")])
choices("Seated cable row or chest-supported row", [("Cable row", "seated_row"), ("Chest-supported", "supported_row")])
choices("Calf raise (seated or standing)", [("Seated", "seated_calf"), ("Standing", "standing_calf")])
profile("clean", "glutes quads traps_upper", "hamstrings calves front_delts", "erectors abs deep_core obliques forearm_flexors", "",
        "Clean phase: hip and knee extension accelerate the bar, then the shoulders support the front-rack catch. The press phase has a different map.")
choices("Barbell Clean and Press", [("Clean phase", "clean"), ("Press phase", "overhead_press")])

# Each region has one name for the diagram and accessible text. Deep structures are
# schematic projections, deliberately explained in the popup and preview.
MUSCLES = {
    "pecs": "Pectoralis major (chest)", "upper_pecs": "Clavicular pectoralis major (upper chest)",
    "front_delts": "Anterior deltoids (front shoulders)", "side_delts": "Lateral deltoids (side shoulders)",
    "rear_delts": "Posterior deltoids (rear shoulders)", "biceps": "Biceps brachii",
    "brachialis": "Brachialis", "triceps": "Triceps brachii", "forearm_flexors": "Forearm / finger flexors (grip)",
    "forearm_extensors": "Forearm / wrist extensors", "brachioradialis": "Brachioradialis (outer forearm)",
    "rotator_cuff": "Rotator cuff (deep shoulder)",
    "traps_upper": "Upper trapezius", "traps_mid": "Middle trapezius", "traps_lower": "Lower trapezius",
    "rhomboids": "Rhomboids (beneath trapezius)", "lats": "Latissimus dorsi (lats)",
    "serratus": "Serratus anterior", "abs": "Rectus abdominis (abs)", "obliques": "Internal / external obliques",
    "deep_core": "Transversus abdominis (deep core)", "erectors": "Erector spinae (spinal extensors)",
    "glutes": "Gluteus maximus", "glute_med": "Gluteus medius / minimus (hip stabilizers)",
    "hip_flexors": "Iliopsoas / hip flexors (deep)", "adductors": "Hip adductors (inner thighs)",
    "quads": "Quadriceps", "hamstrings": "Hamstrings", "calves": "Gastrocnemius (calf)",
    "soleus": "Soleus (deep calf)", "tibialis": "Tibialis anterior (front shin)",
}

# Original anatomy artwork, one half of each symmetrical figure. All paths are
# authored here, shared by Android VectorDrawable and browser SVG output.
SILHOUETTE = "M120,14 C102,14 95,28 97,44 C98,56 104,64 109,67 L108,82 C96,87 86,88 76,94 C60,96 51,109 49,126 L39,176 L28,219 L20,258 L11,281 Q8,291 14,295 L20,281 L18,299 Q21,306 25,299 L30,282 L29,300 Q33,306 36,297 L42,269 L47,244 L58,212 L70,179 L76,155 Q80,172 80,195 L83,234 L78,263 Q74,281 77,304 L81,350 L83,387 Q83,402 87,414 L85,453 Q85,477 91,496 L94,532 L88,548 Q87,556 94,557 L111,557 Q118,556 117,548 L113,529 L117,486 L116,449 L115,414 Q119,401 119,386 L120,321 L121,386 Q121,401 125,414 L124,449 L123,486 L127,529 L123,548 Q122,556 129,557 L146,557 Q153,556 152,548 L146,532 L149,496 Q155,477 155,453 L153,414 Q157,402 157,387 L159,350 L163,304 Q166,281 162,263 L157,234 L160,195 Q160,172 164,155 L170,179 L182,212 L193,244 L198,269 L204,297 Q207,306 211,300 L210,282 L215,299 Q219,306 222,299 L220,281 L226,295 Q232,291 229,281 L220,258 L212,219 L201,176 L191,126 C189,109 180,96 164,94 C154,88 144,87 132,82 L131,67 C136,64 142,56 143,44 C145,28 138,14 120,14 Z"
FRONT = {
    "upper_pecs": "M117,97 Q96,91 79,108 L84,118 Q104,113 117,118 Z",
    "pecs": "M117,121 Q99,114 81,121 L82,140 Q96,155 116,145 Z",
    "front_delts": "M77,99 Q62,99 56,119 L55,139 Q65,137 77,119 Z",
    "side_delts": "M53,124 L51,147 L57,154 L67,141 L70,128 Z",
    "biceps": "M72,145 Q57,145 52,165 L47,190 Q58,194 66,179 L74,158 Z",
    "brachialis": "M70,169 L63,191 L57,201 L65,201 L74,181 Z",
    "triceps": "M49,153 L43,180 L42,196 L47,200 L52,178 L56,154 Z",
    "forearm_flexors": "M51,206 Q43,199 39,217 L29,251 L34,260 L44,237 Z",
    "brachioradialis": "M58,205 L51,207 L44,236 L38,261 L44,254 L55,225 Z",
    "serratus": "M82,149 L94,155 L84,160 Z M83,164 L96,168 L85,175 Z M85,178 L98,181 L87,188 Z",
    "abs": "M102,153 L117,151 L117,172 L101,172 Z M101,177 L117,177 L117,197 L101,197 Z M101,202 L117,202 L117,222 L102,222 Z M103,227 L117,227 L117,250 Q109,250 105,242 Z",
    "obliques": "M87,191 L98,195 L98,235 L103,251 L91,243 L85,223 Z",
    "deep_core": "M88,250 Q102,262 117,256 L117,269 Q101,272 85,263 Z",
    "hip_flexors": "M90,273 L103,278 L113,304 L108,316 L98,299 Z",
    "glute_med": "M81,262 L87,273 L91,294 L80,301 Q76,280 81,262 Z",
    "adductors": "M111,312 L118,325 L116,378 L108,384 L102,356 Z",
    "quads": "M83,310 L96,302 L105,320 L100,354 L106,384 L97,398 L87,383 Z M98,309 L105,313 L108,345 L103,352 Z",
    "tibialis": "M106,421 L112,424 L110,478 L106,514 L102,519 L100,483 Z",
    "calves": "M90,423 L97,424 L97,478 L92,486 L89,466 Z",
    "soleus": "M91,490 L97,484 L100,518 L97,529 Z",
}
BACK = {
    "traps_upper": "M108,80 L117,77 L117,113 L102,111 L82,101 Z",
    "traps_mid": "M103,116 L117,117 L117,145 L106,151 L98,137 Z",
    "traps_lower": "M106,156 L117,151 L117,198 L107,182 Z",
    "rhomboids": "M98,116 L102,138 L105,153 L99,164 L92,149 L91,130 Z",
    "rotator_cuff": "M87,116 L91,132 L85,147 L75,143 L75,127 Z",
    "rear_delts": "M76,100 Q59,101 55,121 L55,143 L73,135 L79,119 Z",
    "side_delts": "M52,126 L48,147 L55,154 L60,143 Z",
    "triceps": "M57,148 L72,144 L74,162 L63,192 L49,200 L47,188 Z",
    "forearm_extensors": "M49,205 L58,203 L54,227 L42,256 L35,262 L39,233 Z",
    "forearm_flexors": "M42,204 L47,207 L37,241 L31,257 L27,252 L33,223 Z",
    "lats": "M81,153 L91,165 L103,187 L105,221 L93,241 L87,221 L83,192 Z",
    "erectors": "M109,193 L117,207 L117,262 L110,268 L105,245 Z",
    "obliques": "M87,231 L101,231 L104,247 L91,258 L82,259 Z",
    "glute_med": "M82,265 Q97,255 108,270 L97,280 L80,282 Z",
    "glutes": "M80,287 Q96,279 114,277 L118,297 L116,319 Q98,334 82,319 Z",
    "adductors": "M114,327 L118,327 L115,373 L109,388 L105,365 Z",
    "hamstrings": "M83,330 Q94,337 104,329 L108,351 L103,379 L104,400 L96,407 L87,393 Z",
    "calves": "M89,423 L99,418 L102,434 L106,418 L113,426 L112,459 L105,483 L101,470 L96,486 L88,459 Z",
    "soleus": "M89,472 L97,490 L100,479 L106,493 L111,471 L108,512 L103,528 L97,528 L93,510 Z",
}
COLORS = dict(primary="#FF7765", assisting="#E9BF59", stabilizers="#67B7F7", inactive="#333E49")


def image_paths(p):
    roles = {m: role for role in ("primary", "assisting", "stabilizers") for m in p[role]}
    # The upper chest is part of the whole pectoralis major; only an explicit
    # upper-chest assignment (incline work) overrides that inherited role.
    if "pecs" in roles and "upper_pecs" not in roles:
        roles["upper_pecs"] = roles["pecs"]
    paths = [("M0,0 H520 V580 H0 Z", "#141B23", "", 0)]
    for offset, regions in ((10, FRONT), (270, BACK)):
        paths.append((SILHOUETTE, "#252F39", "#546372", offset))
        for region, d in regions.items():
            color = COLORS.get(roles.get(region), COLORS["inactive"])
            paths.append((d, color, "#141B23", offset))
            # Reflect original paths to create the other side of the figure.
            paths.append((d, color, "#141B23", (offset, True)))
    return paths


def vector(p):
    root = ET.Element("vector", {"xmlns:android": "http://schemas.android.com/apk/res/android",
        "android:width": "520dp", "android:height": "580dp", "android:viewportWidth": "520", "android:viewportHeight": "580"})
    for d, fill, stroke, transform in image_paths(p):
        attrs = {"android:translateX": str(transform[0] + 240), "android:scaleX": "-1"} if isinstance(transform, tuple) else {"android:translateX": str(transform)}
        group = ET.SubElement(root, "group", attrs)
        ET.SubElement(group, "path", {"android:pathData": d, "android:fillColor": fill,
            "android:strokeColor": stroke or fill, "android:strokeWidth": "1.4", "android:strokeLineJoin": "round"})
    ET.indent(root)
    return ET.tostring(root, encoding="unicode") + "\n"


def svg(p):
    parts = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 520 580">']
    for d, fill, stroke, transform in image_paths(p):
        t = f'translate({transform[0] + 240},0) scale(-1,1)' if isinstance(transform, tuple) else f'translate({transform},0)'
        parts.append(f'<path transform="{t}" d="{d}" fill="{fill}" stroke="{stroke or fill}" stroke-width="1.4" stroke-linejoin="round"/>')
    return "".join(parts) + "</svg>"


def main():
    programs = json.loads((MAIN / "assets/programs.json").read_text(encoding="utf-8"))["programs"]
    names = sorted({i["exercise"] for p in programs for d in p["days"] for i in d["items"]})
    missing = [n for n in names if key(n) not in ASSIGNMENTS]
    assert not missing, f"Assign muscles for: {missing}"
    for id, p in PROFILES.items():
        muscles = p["primary"] + p["assisting"] + p["stabilizers"]
        assert p["primary"], id
        assert len(muscles) == len(set(muscles)), f"Conflicting roles: {id}"
        assert set(muscles) <= MUSCLES.keys(), id
        assert set(muscles) <= FRONT.keys() | BACK.keys(), f"Undrawn muscles: {id}"
    images = MAIN / "res/drawable"
    maps = {}
    for n in names:
        variants = []
        for v in ASSIGNMENTS[key(n)]:
            resource = "mm_" + key(n) + ("_" + v["profile"] if len(ASSIGNMENTS[key(n)]) > 1 else "")
            (images / f"{resource}.xml").write_text(vector(PROFILES[v["profile"]]), encoding="utf-8")
            variants.append(dict(**v, image=resource))
        maps[key(n)] = variants
    library = dict(muscles=MUSCLES, profiles=PROFILES, exercises=maps)
    (MAIN / "assets/muscle_maps.json").write_text(json.dumps(library, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    # Standalone review page: images are inline SVG, no server or CDN required.
    preview = ROOT / "docs/muscle-maps.html"
    preview.parent.mkdir(exist_ok=True)
    data = json.dumps(dict(names=names, **library), ensure_ascii=False).replace("</", "<\\/")
    artwork = json.dumps({id: svg(p) for id, p in PROFILES.items()})
    preview.write_text('''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>GymClues muscle maps</title><style>
*{box-sizing:border-box}body{background:#10161d;color:#e7edf3;font:16px system-ui;margin:0;padding:24px}main{max-width:840px;margin:auto}
h1{font-size:28px;margin-bottom:8px}p{color:#b9c7d4;line-height:1.5}select,button{font:inherit;background:#25313e;color:white;border:1px solid #566879;border-radius:10px;padding:12px;max-width:100%;margin:8px 8px 8px 0}
#art{max-width:520px;margin:auto}svg{display:block;width:100%}.views{display:flex;justify-content:space-around;color:#a7b8c8;letter-spacing:2px;font-size:12px;margin:12px auto;max-width:520px}
.roles{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:12px}.role{border-radius:16px;background:#1b2530;padding:16px;border-top:3px solid var(--c)}h3{color:var(--c);margin:0 0 12px;font-size:15px}li{margin:8px 0}ul{padding-left:18px;font-size:14px}a{color:#67b7f7}.small{font-size:13px}</style>
<main><p>GYMCLUES / EXERCISE ATLAS</p><h1>Muscle mapping</h1><p id="coverage"></p><label for="exercise">Exercise</label><br><select id="exercise"></select><div id="variants"></div><h2 id="title"></h2><p id="kind"></p><div class="views"><span>FRONT</span><span>BACK</span></div><div id="art"></div><div class="roles" id="roles"></div><p id="note"></p>
<p class="small">Typical muscle roles, not measured activation. Technique, support and anatomy change involvement. Surface regions are simplified; deep muscles are projected schematically. The map shows both sides for alternating exercises. Some contributing muscles are omitted.</p><p><a id="source" target="_blank" rel="noreferrer">Anatomy reference</a></p><button id="download">Save this diagram as SVG</button></main><script>
const data=DATA,artwork=ARTWORK;
const el=id=>document.getElementById(id);const key=n=>n.toLowerCase().replace(/[^a-z0-9]+/g,'_').replace(/^_|_$/g,'');let current;
el('coverage').textContent=data.names.length+' exercise names across PROGRAM_COUNT workout programs. Offline vector artwork.';
data.names.forEach(n=>{let o=document.createElement('option');o.textContent=n;el('exercise').append(o)});
function show(index){const name=el('exercise').value;current=data.exercises[key(name)][index];const p=data.profiles[current.profile];el('title').textContent=name;el('kind').textContent=p.kind;el('art').innerHTML=artwork[current.profile];el('note').textContent=p.note;el('source').href=p.source;el('roles').replaceChildren();
[['primary',p.kind==='mobility'?'Mobility targets':p.kind==='recovery'?'Recovery areas':'Targets','#ff7765'],['assisting','Assisting movers','#e9bf59'],['stabilizers','Stabilizers','#67b7f7']].forEach(([role,title,color])=>{let box=document.createElement('section');box.className='role';box.style.setProperty('--c',color);let h=document.createElement('h3');h.textContent=title;box.append(h);let ul=document.createElement('ul');(p[role].length?p[role]:['none']).forEach(m=>{let li=document.createElement('li');li.textContent=data.muscles[m]||'None separately mapped';ul.append(li)});box.append(ul);el('roles').append(box)});}
function select(){el('variants').replaceChildren();const list=data.exercises[key(el('exercise').value)];if(list.length>1)list.forEach((v,i)=>{let b=document.createElement('button');b.textContent=v.label;b.onclick=()=>show(i);el('variants').append(b)});show(0)}
el('exercise').onchange=select;el('download').onclick=()=>{const p=data.profiles[current.profile];let text='<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 740 1100"><rect width="740" height="1100" fill="#141b23"/>';
const escape=s=>s.replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&apos;'}[c]));let y=40;const line=(s,color='#e7edf3')=>{text+='<text x="28" y="'+y+'" font-family="sans-serif" font-size="17" fill="'+color+'">'+escape(s)+'</text>';y+=25};
const name=el('exercise').value;line(name);if(current.label)line(current.label);text+='<g transform="translate(110,80)">'+artwork[current.profile].replace(/<svg[^>]*>|<\\/svg>/g,'')+'</g>';y=690;
[['primary','Targets','#ff7765'],['assisting','Assisting movers','#e9bf59'],['stabilizers','Stabilizers','#67b7f7']].forEach(([role,title,color])=>{line(title,color);let words=p[role].map(m=>data.muscles[m]).join('; ');let row='';words.split(' ').forEach(w=>{if((row+' '+w).length>80){line(row);row=w}else row+=(row?' ':'')+w});line(row||'None separately mapped')});line('Schematic regions; deep muscles projected onto the surface.','#a7b8c8');line('Typical roles; technique changes involvement.','#a7b8c8');text+='</svg>';let a=document.createElement('a');a.href=URL.createObjectURL(new Blob([text],{type:'image/svg+xml'}));a.download=current.image+'.svg';a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000)};select();
</script></html>'''.replace("DATA", data).replace("ARTWORK", artwork).replace("PROGRAM_COUNT", str(len(programs))), encoding="utf-8")
    print(f"Generated {len(maps)} exercise maps ({len(names)} original names), {len(PROFILES)} muscle profiles, {sum(len(v) for v in maps.values())} vector images.")


if __name__ == "__main__":
    main()
