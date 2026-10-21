"""Original schematic setup artwork and model-independent adjustment instructions."""
from xml.etree import ElementTree as ET

GUIDES = {}


def guide(id, scene, steps, caution="", markers=None):
    GUIDES[id] = dict(scene=scene, steps=[dict(title=t, text=b) for t, b in steps], caution=caution,
                      markers=markers or [(195, 245), (330, 205), (340, 295), (520, 245)])


guide("eq_chest_press", "chest_press", [
    ("Seat height", "Unload the arms, release the seat adjustment, and set the handles approximately across mid-chest. Let the lock engage fully."),
    ("Starting reach", "If a start-position control is provided, choose a reach you can hold without the shoulders being pulled far behind you."),
    ("Body support", "Sit with your back supported and feet stable; keep wrists aligned with the handles. Test the path before a working set."),
    ("Resistance", "With the stack resting, insert the correct selector pin fully into one plate. Begin with a light test set.")])
guide("eq_lat_pulldown", "pulldown", [
    ("Seat and thigh pad", "Sit with both feet grounded. Adjust the thigh restraint until it holds the thighs comfortably without crushing them."),
    ("Overhead attachment", "Check the bar and attachment clip. Choose a grip that allows the bar to travel in front of your head."),
    ("Reach and clearance", "Use the intended steps or ask for help if the attachment is out of reach. Do not climb the frame or jump while holding a heavy stack."),
    ("Resistance", "Select a light load with the stack fully down; verify that the thigh restraint keeps you seated.")],
    markers=[(215, 249), (325, 65), (330, 150), (520, 245)])
guide("eq_leg_press", "leg_press", [
    ("Seat distance", "Adjust the seat or backrest so your feet reach the platform and the pelvis stays against the support throughout your chosen depth."),
    ("Foot placement", "Place the whole foot on the platform, approximately hip-to-shoulder width as comfortable. Keep knees tracking with the toes."),
    ("Stop and return", "Identify the re-rack or stop mechanism on your model. Test it lightly; do not force deep knee bend or snap the knees backward."),
    ("Resistance", "Use the selector only with the stack resting. A plate-loaded sled has different catches: follow its placard and get an orientation before use.")],
    markers=[(160, 255), (383, 210), (365, 305), (520, 245)])
guide("eq_smith", "smith", [
    ("Bar hooks", "Practice the rotation that releases and re-engages the hooks using an unloaded or minimally loaded bar. Confirm both sides catch."),
    ("Safety stops", "Place the two stops equally, just below your intended bottom position, so a failed rep cannot trap you. Test before adding plates."),
    ("Bench or stance", "Center a stable bench or your stance under the actual rail path. Inclined rails may require a different position from vertical rails."),
    ("Load and clearance", "Load both sides evenly and use the model's required collars. Clear the path and re-hook securely before stepping away.")],
    "Smith bars have different starting resistance and rail angles. A hook is not a substitute for correctly set safety stops.",
    [(255, 135), (377, 240), (263, 298), (459, 143)])
guide("eq_dumbbells", "dumbbells", [
    ("Choose the pair", "Check the weight markings and handle condition. For two-handed work, use a matched pair unless your exercise intentionally calls for a single weight."),
    ("Adjustable locks", "For adjustable dumbbells, follow the maker's selector or collar procedure and verify every plate is locked before lifting; fixed dumbbells have no adjustment."),
    ("Pickup and start", "Clear space, use a controlled hip and knee bend to collect the weights, and use the exercise's intended start position."),
    ("Support and return", "If a bench is needed, lock it first. Choose a load you can return safely; keep fingers clear when re-racking.")],
    markers=[(165, 145), (290, 155), (210, 285), (460, 260)])
guide("eq_adjustable_bench", "bench", [
    ("Back-pad angle", "Support the pad with no one on the bench. Release the intended pin or ladder and choose flat, incline or upright as the exercise requires."),
    ("Locking mechanism", "Let the pin fully seat or the support bar fully enter the ladder notch. Gently test the pad before adding bodyweight or dumbbells."),
    ("Seat position", "Adjust and lock the seat if the model allows it, so you are supported without sliding. Keep fingers away from hinges."),
    ("Floor and clearance", "Set the bench on a stable, level surface with all feet contacting the floor. Respect its combined user-and-load limit.")],
    "The back-pad angle is exercise-specific; no single notch fits every movement. An adjustable bench is not automatically rated as a step-up platform.",
    [(235, 135), (265, 239), (373, 220), (432, 325)])
guide("eq_x_cable", "cable", [
    ("Pulley height", "With the stack resting and cable unloaded, support the carriage, release its adjustment, slide to the required height, then check the lock has engaged."),
    ("Attachment clip", "Use an attachment rated for the station. Close its clip fully and check that the cable end, handle or ankle cuff is secure."),
    ("Weight selector", "Insert the station's selector pin fully with the stack down. For two-arm exercises, check each stack independently; pulley ratios can differ."),
    ("Line of pull", "Position your body so the cable runs freely and does not rub against you, the frame or another cable. Test the full range with a light load.")],
    "High pulley: pushdowns and pulldowns. Low pulley: many curls and pull-throughs. Chest height: many Pallof presses. The exact exercise setup takes priority.",
    [(190, 165), (315, 183), (496, 250), (340, 294)])
guide("eq_x_barbell", "rack", [
    ("J-hook height", "Set both hooks to the same height so the bar can be unracked without a tiptoe reach or deep squat. Check each hook is fully secured."),
    ("Safety arms", "Set both safeties equally for your squat or bench range; test with a light bar that they catch it without trapping your body."),
    ("Plates and collars", "Load compatible plates evenly on both sides and secure the required collars. Use the rack and bar within their rated capacity."),
    ("Bench and exit", "For bench pressing, center and lock a suitable bench; arrange appropriate spotting. Rehearse the unrack and re-rack before working sets.")],
    markers=[(225, 135), (385, 235), (470, 135), (270, 303)])
guide("eq_x_ez_bar", "ez_bar", [
    ("Grip bends", "Choose matching angled grip positions that keep the wrists comfortable. Center the hands equally around the middle of the bar."),
    ("Sleeves and collars", "Identify whether the bar is fixed-weight or plate-loaded. For plate-loaded bars, fit compatible plates and secure collars on both ends."),
    ("Starting support", "Use a stable rack, bench or controlled floor pickup appropriate to the exercise. Keep the area around the bar clear."),
    ("Light rehearsal", "Practice the curl or extension path and a safe return before using a working load; do not carry a loaded bar over someone's head.")],
    markers=[(280, 145), (440, 145), (195, 290), (385, 290)])
guide("eq_x_trap_bar", "trap_bar", [
    ("Handle choice", "Select the intended high or low handles, if the bar offers both, before loading. Different handle heights change the starting range."),
    ("Even loading", "Load both sleeves equally, fit compatible collars and check that the plates and handles are secure."),
    ("Stance", "Stand centered inside the frame with hands evenly placed. Keep feet and shins clear of the frame as you hinge down."),
    ("Clear landing", "Lift and set down in a clear, level area. Return the bar evenly without trapping the toes beneath the plates or frame.")],
    markers=[(225, 180), (480, 200), (325, 266), (390, 320)])
guide("eq_x_45_degree_extension_bench", "extension_bench", [
    ("Hip-pad height", "Adjust the pad so its upper edge supports the upper thighs just below the hip crease, allowing the hips to hinge freely."),
    ("Foot restraints", "Place feet on the platform and fit the ankle or calf restraints as shown on the placard. Check they hold without painful pressure."),
    ("Range", "Start with bodyweight. Hinge to a comfortable depth and return to a straight, controlled body line without throwing the back upward."),
    ("Entry and exit", "Use the machine's handles to mount and leave the bench. Confirm the pad adjustment is locked before leaning forward.")],
    markers=[(300, 185), (405, 302), (185, 150), (265, 280)])
guide("eq_x_abdominal_crunch_machine", "crunch", [
    ("Seat and pivot", "Adjust the seat to the machine's torso-alignment markers; sit with pelvis and feet supported."),
    ("Upper-body pad", "Fit the chest, shoulder or arm pad as your model directs. Do not press a pad against your neck."),
    ("Start range", "Select a start position you can reach without being forced into a large back arch. Practice the curl without momentum."),
    ("Resistance", "Choose light resistance with the stack resting; use trunk control instead of pulling hard with the arms.")])
guide("eq_x_assisted_dip_machine", "assisted_dip", [
    ("Assistance", "On counterweighted machines, more selected weight usually means more assistance. Check the placard; this is different from a normal resistance stack."),
    ("Handles and platform", "Set adjustable handle width if provided. Locate the steps and moving knee or foot platform before mounting."),
    ("Controlled mounting", "Hold the handles, step onto the intended supports and transfer weight to the assistance platform gradually. Keep clear of its travel."),
    ("Controlled dismount", "Let the platform return under control, using the intended steps. Do not jump off or change the selector while it is moving.")],
    markers=[(510, 245), (263, 142), (270, 262), (355, 323)])
guide("eq_x_assisted_pull_up_machine", "assisted_pullup", [
    ("Assistance", "Check the placard: a heavier selection usually provides more assistance on a counterweight machine. Start with enough help to control the full rep."),
    ("Overhead grip", "Choose reachable handles and a comfortable grip. Use the intended steps rather than climbing the frame."),
    ("Knee or foot support", "Mount as shown, holding the handles while you transfer weight onto the moving support. Keep fingers clear of its mechanism."),
    ("Dismount", "Return the platform slowly and step onto the designated steps before releasing the handles. Keep the selector unchanged while moving.")],
    markers=[(510, 245), (280, 60), (270, 265), (355, 323)])
guide("eq_x_hack_squat_machine", "hack_squat", [
    ("Back and shoulder pads", "Fit under the shoulder pads with your back supported; adjust the pad or start height if your model allows it."),
    ("Foot platform", "Place feet securely on the platform in a comfortable stance; rehearse a depth that keeps heels and pelvis supported."),
    ("Safety catches", "Locate the release handles and set any adjustable stops before loading. Practice releasing and re-racking with minimal resistance."),
    ("Even loading", "Add plates evenly and use the specified retention system. Never move a safety stop with the carriage loaded on it.")],
    markers=[(230, 128), (383, 298), (350, 220), (470, 180)])
guide("eq_x_hip_abduction_machine", "abduction", [
    ("Seat and backrest", "Sit with your pelvis and back supported; adjust the seat if available and place feet on the intended supports."),
    ("Pad orientation", "For abduction, the pads resist the outside of the knees or thighs as you move outward. Follow the model's conversion instructions if shared with adduction."),
    ("Starting width", "Use the unloaded range lever to choose a comfortable, narrower starting position. Release it only once the setting is engaged."),
    ("Resistance", "Select light resistance, use the handles for stability and keep the pelvis still as the legs move apart.")])
guide("eq_x_hip_adduction_machine", "adduction", [
    ("Seat and backrest", "Adjust for a supported torso and stable feet, using the machine's designated footrests."),
    ("Pad orientation", "For adduction, pads contact the inside of the knees or thighs as you bring the legs together. Verify any dual-function conversion is locked."),
    ("Starting width", "Choose a comfortable starting opening with the unloaded range lever; do not force a wide groin stretch."),
    ("Resistance", "Use a light stack selection and bring the pads inward slowly without bouncing or letting the stack crash.")])
guide("eq_x_incline_chest_press_machine", "incline_press", [
    ("Seat height", "Adjust the unloaded seat so the handles meet the upper-chest pressing line without forcing the elbows far behind you."),
    ("Starting handles", "Set a comfortable start position if the model offers one, and check the entry or start-assist lever."),
    ("Back and feet", "Keep the back against the inclined support and the feet stable; test that the handle path clears your shoulders comfortably."),
    ("Resistance", "Select a light stack setting, or load plate horns evenly as the placard requires. Keep clear of moving lever arms.")])
guide("eq_x_lateral_raise_machine", "lateral_raise", [
    ("Seat height", "Adjust the seat so your shoulders align with the machine's indicated movement axis."),
    ("Arm pads", "Fit the pads against the intended part of the upper arms near the elbows, not against the neck or wrists."),
    ("Range and posture", "Sit supported and test a comfortable side-raise range without shrugging; adjust a start stop if provided."),
    ("Resistance", "Choose a small load and raise both arms smoothly without driving against the pads with the trunk.")],
    markers=[(245, 270), (355, 167), (205, 121), (520, 245)])
guide("eq_x_leg_extension_machine", "leg_extension", [
    ("Back-pad position", "Move the back pad so the thighs rest on the seat and the knee joint lines up with the machine pivot."),
    ("Knee alignment", "Check the side of the knee against the pivot marker. Do not use the kneecap as a pad or pressure point."),
    ("Shin roller and start", "Fit the roller across the lower shin above the ankle, clear of the foot. Set a comfortable knee-bend starting position."),
    ("Resistance", "Select a light load and test smooth knee extension without kicking, lifting the hips or snapping backward.")],
    markers=[(193, 181), (330, 240), (345, 291), (520, 245)])
guide("eq_x_lumbar_extension_machine", "lumbar", [
    ("Seat and pelvic restraint", "Adjust the seat and any lap or thigh restraint so the pelvis is held as the placard specifies."),
    ("Back pad", "Fit the moving pad against the intended upper-back region, never directly against the neck."),
    ("Start angle", "Choose a small, comfortable starting range; do not force spinal movement to reach a preset endpoint."),
    ("Resistance", "Begin with a light selection and controlled movement. Get individualized guidance if spinal loading is restricted for you.")])
guide("eq_x_lying_leg_curl_machine", "lying_curl", [
    ("Knee position", "Lie on the bench so the knee joint lines up with the machine pivot and the thighs are supported."),
    ("Ankle roller", "Adjust the roller onto the lower calf just above the heel, avoiding direct pressure on the Achilles tendon."),
    ("Hip support", "Keep the pelvis on the pad and select a comfortable start range. Hold the intended handles without pulling the back into a large arch."),
    ("Resistance", "Select a light load and test a smooth curl before loading further; keep clear of the moving leg lever.")],
    markers=[(352, 235), (415, 285), (245, 212), (520, 245)])
guide("eq_x_pec_deck_machine", "pec_deck", [
    ("Seat height", "Set the seat so handles or forearm pads align with the intended chest-fly path, approximately across chest level."),
    ("Arm start position", "Adjust the unloaded arms so you can reach them without being pulled into an extreme shoulder stretch."),
    ("Chest or reverse mode", "If the machine converts to reverse fly, follow its conversion instructions and face the correct support pad for that mode."),
    ("Resistance", "Start with a light selection and keep the torso supported throughout the opening and closing path.")],
    markers=[(245, 270), (370, 145), (245, 175), (520, 245)])
guide("eq_x_preacher_curl_machine", "preacher", [
    ("Seat height", "Adjust the seat so the upper arms rest comfortably on the pad without shrugging the shoulders."),
    ("Elbow pivot", "Align the elbows with the machine's indicated rotation axis. The pad supports the upper arms, not a painfully compressed elbow point."),
    ("Grip and range", "Choose a wrist-friendly grip and a start position that does not force the elbows into full extension."),
    ("Resistance", "Start light and test a slow curl and return while the upper arms remain supported.")],
    markers=[(195, 245), (315, 182), (375, 220), (520, 245)])
guide("eq_x_seated_calf_raise_machine", "seated_calf", [
    ("Seat and feet", "Place the balls of the feet securely on the step, with room for a small controlled heel drop."),
    ("Thigh restraint", "Fit the pad just above the knees, snug enough to transfer the load without pressing painfully on the kneecaps."),
    ("Release and re-rack", "With a light load, raise enough to disengage the latch as the placard directs. Practice re-engaging it before the set."),
    ("Resistance", "Load both sides evenly if plate-loaded, or set the stack pin fully. Do not bounce the heels at the bottom.")],
    markers=[(355, 308), (300, 225), (372, 270), (520, 245)])
guide("eq_x_seated_leg_curl_machine", "seated_curl", [
    ("Backrest", "Adjust the back support until the knee joints align with the machine pivot and the thighs are supported."),
    ("Thigh restraint", "Fit the upper restraint above the knees to hold the thighs gently against the seat; do not clamp the kneecaps."),
    ("Lower-leg roller", "Place the lower roller on the lower calf above the heel. Choose a start angle you can reach comfortably."),
    ("Resistance", "Select light resistance and check that the pelvis stays supported during both curl and return.")],
    markers=[(193, 181), (300, 225), (365, 278), (520, 245)])
guide("eq_x_seated_row_machine", "row", [
    ("Seat and chest pad", "Adjust the seat and any chest support so you can reach the handles without rounding or lifting off the pad."),
    ("Handle path", "Choose a grip that allows a comfortable elbow path toward the ribs; keep wrists aligned with the handles."),
    ("Foot support", "Place the feet on the intended braces and keep the torso steady. A chest-supported model reduces the need to hold the torso upright."),
    ("Resistance", "Use light resistance to test the entire reach and pull before adding load; keep hands clear of lever hinges.")])
guide("eq_x_shoulder_press_machine", "shoulder_press", [
    ("Seat height", "Set the unloaded seat so the start handles are around shoulder level and you can grip them without forcing the shoulders backward."),
    ("Grip", "Choose the provided neutral or overhand handles that allow comfortable wrist and elbow alignment."),
    ("Back support", "Keep the trunk against the pad and feet stable. Test the overhead path without a large lower-back arch."),
    ("Resistance", "Select a light stack setting or load both plate horns evenly. Confirm a controlled return before a working set.")],
    markers=[(195, 245), (325, 105), (205, 170), (520, 245)])
guide("eq_x_standing_calf_raise_machine", "standing_calf", [
    ("Shoulder-pad height", "Adjust the unloaded pad or start position so you can enter and stand tall without ducking under a heavily loaded pad."),
    ("Forefoot placement", "Keep the balls of both feet securely on the platform; leave only a comfortable amount of heel overhang."),
    ("Latch and support", "Identify the release and re-rack control, use the intended handles, and practice with a light load."),
    ("Resistance", "Fit the selector fully or load plate horns evenly. Keep the knees controlled rather than bouncing them with each rep.")],
    markers=[(260, 125), (320, 320), (378, 230), (520, 245)])
guide("eq_x_torso_rotation_machine", "rotation", [
    ("Seat and lower-body pads", "Fit the seat and leg or thigh pads so the lower body is supported as the model intends."),
    ("Upper-body handles", "Hold the provided handles without twisting the neck or pulling the shoulders into an awkward position."),
    ("Starting angle", "Choose a modest rotation range using the unloaded range control. Do not force the trunk to the machine's maximum stop."),
    ("Resistance", "Start light and rotate slowly; reverse direction using the model's instructions rather than twisting against a fixed restraint.")])
guide("eq_x_triceps_extension_machine", "triceps", [
    ("Seat height", "Set the seat so upper arms rest on the intended pad without the shoulders rising."),
    ("Elbow axis", "Align the elbow joints with the machine's pivot markers and keep pressure on the upper-arm pad, not the elbow tips."),
    ("Handles and range", "Grip with wrists aligned and select a comfortable start angle without forcing the elbows into a deep bend."),
    ("Resistance", "Use a light selector setting and test elbow extension and return without lifting the upper arms or snapping into lockout.")],
    markers=[(195, 245), (315, 182), (375, 220), (520, 245)])

# All artwork is authored as paths and converted to native Android vectors and SVG.
BG, FRAME, PAD, BODY, ACCENT, ARROW = "#111A23", "#526779", "#7CE4AE", "#91B5D9", "#FFD18A", "#67B7F7"


def drawing(scene):
    parts = []

    def path(d, color=FRAME, width=8, fill="none"):
        parts.append((d, fill, color, width))

    def rect(x, y, w, h, color=PAD):
        path(f"M{x},{y} h{w} v{h} h{-w} Z", color, 2, color)

    def circle(x, y, r, color=BODY, fill="none", width=6):
        path(f"M{x-r},{y} a{r},{r} 0 1,0 {2*r},0 a{r},{r} 0 1,0 {-2*r},0", color, width, fill)

    def arrow(x, y, horizontal=False):
        if horizontal:
            path(f"M{x-24},{y} h48 M{x-24},{y} l8,-6 M{x-24},{y} l8,6 M{x+24},{y} l-8,-6 M{x+24},{y} l-8,6", ARROW, 3)
        else:
            path(f"M{x},{y-24} v48 M{x},{y-24} l-6,8 M{x},{y-24} l6,8 M{x},{y+24} l-6,-8 M{x},{y+24} l6,-8", ARROW, 3)

    def stack():
        path("M470,330 V65 H562 V330 M455,332 H575", FRAME, 7)
        for y in range(190, 297, 15): rect(488, y, 53, 10, "#314756")
        path("M510,245 H555", ACCENT, 5)
        circle(550, 245, 5, ACCENT, ACCENT, 2)

    def seated():
        path("M100,335 H405 M195,330 V250 M290,330 V250 M155,322 L190,145", FRAME)
        rect(180, 130, 20, 107)
        rect(200, 241, 108, 15)
        circle(234, 98, 19)
        path("M236,119 L240,195 L260,228 L325,238 L340,304 L363,310", BODY, 10)
        arrow(170, 252)

    path("M45,350 H592", "#283B4A", 2)
    if scene == "treadmill":
        # Equipment-only views keep adjustment points visible beneath the callouts.
        path("M105,295 L403,247 L545,279 L242,335 Z", FRAME, 5, "#263744")
        path("M139,292 L401,257 L510,280 L245,322 Z", "#8195A5", 2, "#17232D")
        for x in (205, 255, 305, 355, 405):
            path(f"M{x},303 l-22,-10", "#344957", 2)
        path("M106,307 L242,347 L545,291 M250,345 V351 M514,298 V323", FRAME, 7)
        path("M403,246 L419,125 M505,267 L492,121", FRAME, 11)
        path("M419,150 L326,168 M493,149 L549,169", "#91A4B6", 8)
        path("M408,79 H505 L526,118 H424 Z", FRAME, 4, "#263744")
        path("M425,87 H491 L504,107 H438 Z", PAD, 2, "#183D32")
        path("M442,95 H461 M472,95 H488", PAD, 3)
        circle(507, 113, 5, "#E78780", "#E78780", 2)
        path("M505,120 Q483,152 459,165", "#E78780", 2)
        path("M470,88 V55 H548 M453,166 H565 M182,286 L136,238 H76 M518,298 L566,327", ACCENT, 2)
        arrow(185, 307)
        return parts
    elif scene == "stairs":
        path("M229,326 L365,149 L462,171 L338,338 Z", FRAME, 5, "#263744")
        path("M229,326 H278 V289 H307 V252 H336 V215 H365 V178 H408", "#91A4B6", 5)
        path("M278,289 L370,307 M307,252 L397,270 M336,215 L424,233 M365,178 L453,195", PAD, 5)
        path("M229,336 H343 L464,174 V325 H430 M208,344 H473", FRAME, 8)
        path("M278,251 L282,134 L391,83 M421,190 L419,118 L482,94", "#91A4B6", 7)
        path("M391,77 L452,70 L478,98 L415,108 Z", FRAME, 4, "#263744")
        path("M405,81 L448,78 L462,92 L419,100 Z", PAD, 2, "#183D32")
        circle(459, 102, 4, "#E78780", "#E78780", 2)
        path("M209,344 H172 V314 M322,236 H198 M431,85 V45 H520 M462,102 H526 V145", ACCENT, 2)
        arrow(371, 263)
        return parts
    elif scene == "bike":
        circle(424, 270, 59, FRAME, "#263744", 5)
        circle(424, 270, 42, "#344957", width=2)
        path("M200,336 H498 M228,330 L284,213 L345,277 L421,268 L369,180 L407,139", FRAME, 9)
        path("M284,213 L369,180 M284,213 V167", "#91A4B6", 7)
        path("M253,160 Q273,152 305,161 L307,171 H251 Z", PAD, 3, "#183D32")
        path("M407,141 L389,128 L429,109 L457,126", PAD, 7)
        rect(420, 91, 55, 27, "#263744")
        path("M430,98 H465 V109 H430 Z", PAD, 2, "#183D32")
        circle(345, 277, 13, "#91A4B6", width=3)
        path("M345,277 L312,301 M345,277 L378,253 M298,301 H327 M366,253 H391", ACCENT, 4)
        path("M299,298 Q310,286 322,298", PAD, 3)
        circle(361, 207, 6, ACCENT, ACCENT, 2)
        path("M284,187 H190 V133 M410,133 H522 M361,207 H513 V253 M451,100 V49 H515", ACCENT, 2)
        arrow(265, 207)
        return parts
    elif scene == "rower":
        path("M105,327 H179 M465,319 L488,343 H539", FRAME, 7)
        path("M165,282 H520 L527,299 H172 Z", FRAME, 4, "#263744")
        path("M190,288 H506", "#91A4B6", 3)
        circle(137, 247, 65, FRAME, "#263744", 5)
        circle(137, 247, 49, "#344957", width=2)
        for x, y in ((105, 218), (129, 202), (163, 211), (178, 240), (165, 275), (129, 291), (100, 270)):
            path(f"M137,247 L{x},{y}", "#344957", 2)
        rect(333, 264, 69, 14, "#183D32")
        path("M332,264 Q366,252 402,264 M346,280 V286 M390,280 V286", PAD, 4)
        path("M221,274 L244,211 L266,218 L245,282 Z", "#91A4B6", 3, "#263744")
        path("M234,236 L256,243 M227,256 L249,263", PAD, 5)
        path("M156,190 L199,126", FRAME, 6)
        rect(181, 102, 62, 35, "#263744")
        path("M190,111 H233 V127 H190 Z", PAD, 2, "#183D32")
        path("M176,214 L280,201 M270,196 L297,211", ACCENT, 3)
        path("M175,223 L170,238", ACCENT, 5)
        path("M244,240 V186 H313 M367,264 V215 H459 M172,233 L91,185 H65 M211,103 V57 H309", ACCENT, 2)
        arrow(373, 307, True)
        return parts
    if scene in {"bench", "extension_bench"}:
        if scene == "bench":
            path("M155,330 H468 M225,326 L279,239 L400,327 M278,239 L218,128", FRAME, 9)
            path("M193,96 L282,228", PAD, 22)
            path("M298,234 H388", PAD, 19)
            path("M226,279 L298,258 L330,278", ACCENT, 7)
            for x in (245, 266, 287, 308): path(f"M{x},273 v9", BG, 3)
            circle(284, 236, 10, ACCENT)
            path("M235,218 Q208,200 205,170 M205,170 l-7,9 M205,170 l10,3", ARROW, 3)
            arrow(371, 200)
        else:
            path("M166,330 H470 M244,326 L335,190 L438,330 M321,216 L409,305", FRAME)
            path("M280,180 L330,214", PAD, 24)
            path("M385,302 L430,325", PAD, 14)
            circle(189, 132, 17)
            path("M207,145 L280,186 L337,235 L391,295", BODY, 10)
            rect(395, 283, 35, 14)
            arrow(290, 240)
            path("M156,187 Q175,160 201,165", ARROW, 3)
        return parts
    if scene in {"rack", "smith"}:
        path("M130,340 H505 M185,337 V50 H413 V337 M161,337 V80 M438,337 V80", FRAME, 10)
        path("M143,135 H470", PAD, 9)
        for x in (183, 413):
            for y in range(90, 290, 25): circle(x, y, 3, "#9BB1C3", "#9BB1C3", 1)
        path("M190,132 l20,0 v-10 M407,132 h-20 v-10", ACCENT, 5)
        path("M187,240 H265 M411,240 H330", ACCENT, 9)
        for x in (145, 447): rect(x, 105, 16, 60, "#334E60")
        path("M225,288 H348", PAD, 15)
        path("M235,298 V335 M337,298 V335", FRAME, 6)
        arrow(214, 205)
        if scene == "smith":
            path("M165,70 V320 M437,70 V320", "#91A4B6", 4)
            path("M305,110 Q325,105 328,124 M328,124 l-8,-4 M328,124 l4,-8", ARROW, 3)
        return parts
    if scene in {"dumbbells", "ez_bar", "trap_bar"}:
        if scene == "trap_bar":
            path("M180,188 L247,128 H389 L455,188 L389,255 H247 Z M105,188 H180 M455,188 H540", FRAME, 10)
            path("M224,167 V213 M410,167 V213", PAD, 10)
            rect(116, 151, 18, 75, "#334E60"); rect(502, 151, 18, 75, "#334E60")
            path("M314,287 v34 M352,287 v34", BODY, 10)
            arrow(207, 152)
        else:
            if scene == "dumbbells":
                for x in (145, 340):
                    path(f"M{x},150 h100", PAD, 9)
                    for xx in (x, x+80): rect(xx, 117, 19, 65, "#334E60")
                path("M112,300 L125,246 H485 L504,300 M134,300 H488", FRAME)
                arrow(283, 147, True)
            else:
                path("M115,151 H205 L229,136 L263,167 L300,137 L335,166 L372,150 H515", PAD, 9)
                for x in (143, 466): rect(x, 117, 16, 65, "#334E60")
                path("M145,310 L165,255 H455 L475,310", FRAME)
                arrow(445, 190, True)
        return parts
    stack()
    if scene == "cable":
        path("M165,335 V55 H520 M190,75 V315", FRAME, 10)
        for y in range(85, 310, 20): circle(190, y, 3, "#91A4B6", "#91A4B6", 1)
        rect(170, 145, 40, 36, PAD)
        circle(199, 163, 9, BG)
        path("M209,163 L304,185 L337,212", "#B6C8D8", 3)
        path("M305,175 L320,188 L311,201 L298,188 Z", ACCENT, 4)
        path("M329,202 L350,219", PAD, 8)
        arrow(142, 164)
        circle(373, 164, 17)
        path("M373,184 L366,247 L335,298 L327,325 M366,247 L397,292 L401,325 M373,200 L340,212 L318,190", BODY, 9)
        return parts
    if scene in {"assisted_pullup", "assisted_dip"}:
        path("M147,335 V62 H390 V335 M145,335 H430", FRAME, 10)
        path("M215,265 H321", PAD, 14)
        path("M346,327 H402 M341,306 H390", FRAME, 8)
        circle(269, 111, 18)
        path("M270,132 V211 L246,250 M270,211 L299,250", BODY, 10)
        if scene == "assisted_pullup":
            path("M205,63 H335", PAD, 10)
            path("M269,145 L220,110 L219,67 M270,145 L319,108 L320,67", BODY, 8)
        else:
            path("M207,153 H240 M300,153 H340", PAD, 10)
            path("M269,146 L225,170 L221,153 M269,146 L315,171 L320,153", BODY, 8)
        arrow(192, 270)
        return parts
    if scene == "hack_squat":
        path("M105,336 H445 M155,328 L343,58 M240,332 L420,70", FRAME, 10)
        path("M205,183 L285,78", PAD, 22)
        path("M190,109 L248,142", PAD, 17)
        path("M309,319 L401,282", PAD, 18)
        circle(242, 83, 17)
        path("M230,107 L213,183 L287,238 L329,291", BODY, 10)
        path("M271,200 L361,217", ACCENT, 8)
        rect(424, 154, 20, 62, "#334E60")
        arrow(176, 240)
        return parts
    if scene == "lying_curl":
        path("M107,330 H441 M155,330 V245 M359,330 V245", FRAME)
        path("M133,236 L239,216 L359,241", PAD, 18)
        circle(141, 186, 17)
        path("M163,194 L244,205 L351,224 L404,276", BODY, 10)
        circle(356, 236, 10, ACCENT)
        path("M355,236 L416,290", FRAME, 7)
        path("M401,285 L430,294", PAD, 16)
        arrow(340, 278)
        return parts
    if scene in {"pec_deck", "abduction", "adduction", "lateral_raise"}:
        path("M120,335 H420 M270,330 V180", FRAME)
        rect(230, 150, 75, 100); rect(218, 263, 100, 15)
        circle(269, 109, 19)
        path("M269,131 V231 M267,231 L225,270 L210,322 M272,231 L318,270 L335,322", BODY, 10)
        if scene in {"abduction", "adduction"}:
            for x in (203, 325): rect(x, 248, 18, 40)
            path("M197,271 H165 M165,271 l9,-7 M165,271 l9,7 M350,271 H383 M383,271 l-9,-7 M383,271 l-9,7", ARROW, 3)
            if scene == "adduction":
                path("M225,271 H247 M247,271 l-8,-6 M247,271 l-8,6 M316,271 H293 M293,271 l8,-6 M293,271 l8,6", ACCENT, 3)
        else:
            path("M270,151 L217,170 L172,139 M270,151 L323,170 L368,139", BODY, 9)
            path("M160,112 V174 M379,112 V174", PAD, 13)
            path("M155,105 Q269,42 386,105", FRAME, 7)
            arrow(190, 221)
        arrow(349, 276)
        return parts
    if scene == "leg_press":
        path("M80,330 H437 M119,326 L173,243 L233,315 M365,85 L392,317", FRAME, 10)
        path("M123,182 L190,253 L250,253", PAD, 20)
        path("M377,155 L403,264", PAD, 19)
        circle(172, 148, 18)
        path("M181,170 L220,223 L282,195 L379,211", BODY, 10)
        arrow(189, 290, True)
        path("M310,306 H362", ACCENT, 8)
        return parts
    if scene == "standing_calf":
        path("M151,335 H407 M192,330 V70 H378 V330", FRAME, 10)
        rect(225, 129, 106, 18)
        circle(275, 96, 18)
        path("M276,117 V212 L257,299 L252,318 M277,212 L302,299 L307,318", BODY, 10)
        path("M232,328 H335", PAD, 14)
        path("M340,231 H382", ACCENT, 7)
        arrow(207, 162)
        return parts
    seated()
    if scene in {"leg_extension", "seated_curl", "seated_calf"}:
        circle(330, 240, 10, ACCENT)
        if scene == "leg_extension":
            path("M330,240 L345,294", FRAME, 8); path("M327,289 H360", PAD, 17)
            arrow(372, 270)
        elif scene == "seated_curl":
            path("M325,240 L374,282", BODY, 10); path("M355,278 H386", PAD, 18)
            path("M272,220 H322", PAD, 15); arrow(390, 245)
        else:
            path("M271,220 H322", PAD, 17); path("M324,320 H374", PAD, 14)
            path("M324,239 L369,270", FRAME, 7); arrow(379, 300)
    elif scene in {"preacher", "triceps"}:
        path("M261,164 L315,184 L366,215", BODY, 9)
        path("M264,198 L335,218", PAD, 21)
        circle(315, 182, 10, ACCENT)
        path("M315,182 L371,220", FRAME, 6); arrow(385, 251)
    elif scene == "pulldown":
        path("M155,65 H395 M325,65 V105", FRAME, 9)
        path("M255,69 H391", PAD, 8)
        path("M241,143 L283,123 L289,74", BODY, 9)
        path("M258,230 H302", PAD, 16)
        arrow(320, 151)
    elif scene == "shoulder_press":
        path("M241,144 L294,154 L315,106", BODY, 9)
        path("M313,105 H350 M343,106 L374,184", PAD, 9)
        arrow(365, 125)
    elif scene == "row":
        path("M242,145 L275,179 L332,192", BODY, 9)
        rect(281, 151, 15, 70)
        path("M336,173 V211 M337,194 H447", PAD, 8)
        path("M356,304 l15,-26", PAD, 10)
        arrow(377, 219, True)
    elif scene in {"crunch", "lumbar", "rotation"}:
        path("M239,144 L284,169 L322,151", BODY, 9)
        path("M213,154 L253,166", PAD, 18)
        if scene == "rotation":
            path("M265,283 Q318,310 361,271 M361,271 l-12,1 M361,271 l-3,11", ARROW, 3)
        else:
            path("M291,136 Q330,172 310,207 M310,207 l1,-12 M310,207 l11,-5", ARROW, 3)
    else:
        path("M240,145 L283,184 L337,176", BODY, 9)
        path("M331,158 V192 M336,175 L398,151", PAD, 9)
        if scene == "incline_press": path("M170,138 L217,237", PAD, 18)
        arrow(355, 210, True)
    return parts


DIGITS = {
    1: "M-4,-3 L0,-7 V8 M-4,8 H5",
    2: "M-6,-3 Q-6,-9 0,-9 Q7,-9 6,-3 Q5,1 -6,8 H7",
    3: "M-6,-7 Q7,-12 6,-3 Q5,0 0,0 Q8,0 6,7 Q1,12 -6,7",
    4: "M4,9 V-9 L-7,3 H8",
}


def full_paths(g):
    parts = [("M0,0 H640 V400 H0 Z", BG, BG, 0)] + drawing(g["scene"])
    for i, (x, y) in enumerate(g["markers"], 1):
        # White numbered callouts are baked into the vectors, not font-dependent.
        d = f"M{x-18},{y} a18,18 0 1,0 36,0 a18,18 0 1,0 -36,0"
        parts.append((d, "#203C30", ACCENT, 2))
        parts.append((DIGITS[i], "none", "#FFFFFF", 2.2, (x, y)))
    return parts


def vector(g):
    root = ET.Element("vector", {"xmlns:android": "http://schemas.android.com/apk/res/android",
        "android:width": "640dp", "android:height": "400dp", "android:viewportWidth": "640", "android:viewportHeight": "400"})
    for item in full_paths(g):
        d, fill, stroke, width, *shift = item
        group = ET.SubElement(root, "group", {"android:translateX": str(shift[0][0]), "android:translateY": str(shift[0][1]),
            "android:scaleX": "1.2", "android:scaleY": "1.2"}) if shift else root
        ET.SubElement(group, "path", {"android:pathData": d, "android:fillColor": fill if fill != "none" else "#00000000",
            "android:strokeColor": stroke, "android:strokeWidth": str(width), "android:strokeLineCap": "round", "android:strokeLineJoin": "round"})
    ET.indent(root)
    return ET.tostring(root, encoding="unicode") + "\n"


def svg(g):
    parts = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 400">']
    for d, fill, stroke, width, *shift in full_paths(g):
        transform = f' transform="translate({shift[0][0]},{shift[0][1]}) scale(1.2)"' if shift else ""
        parts.append(f'<path d="{d}" fill="{fill}" stroke="{stroke}" stroke-width="{width}" stroke-linecap="round" stroke-linejoin="round"{transform}/>')
    return "".join(parts) + "</svg>"
