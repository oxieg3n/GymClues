"""Authored exercise coaching content; explicit movement families, not runtime keyword guesses."""
PROFILES = {}


def family(ids, safety, prevention, mistakes):
    entry = dict(safetyTips=safety.split("|"), injuryPrevention=prevention.split("|"), mistakes=mistakes.split("|"))
    for id in ids.split():
        assert id not in PROFILES, id
        PROFILES[id] = entry.copy()


family("bench incline decline close_press",
       "Use a load you can bring into position and return safely; arrange appropriate spotting for heavy presses.|Keep the load balanced above the wrists and forearms; stop the set before control is lost.",
       "Rehearse the setup and exit with a light load before working sets.|Use a comfortable shoulder depth, with your upper back supported and feet stable.",
       "Flaring elbows straight sideways: bring them to a comfortable angle below shoulder height.|Bouncing out of the bottom: lower smoothly and reverse under control.|Lifting the hips to finish: reduce the load and keep the same body position.")
family("machine_press machine_incline",
       "Adjust the seat so the handles match the pressing line across your chest.|Use the start-assist lever only as shown on the machine placard; keep clear of the moving arms.",
       "Test the handle path with a light selection before increasing resistance.|Limit the return before your shoulders are pulled into an uncomfortable stretch.",
       "Seat too low or high: adjust until wrists and forearms line up with the handles.|Shoulders rising toward ears: keep the neck relaxed and back supported.|Letting the stack crash: slow the return and keep the handles controlled.")
family("pushup close_pushup",
       "Choose a floor or elevated-hand position you can support with stable wrists.|Keep your trunk controlled as one unit; use an easier version if the hips sag.",
       "Use a fixed, nonslip support for elevated push-ups.|Progress range and repetitions before choosing a harder body angle.",
       "Hips sagging: brace the abdomen and shorten the set.|Elbows spread straight sideways: use a comfortable diagonal path.|Head reaching the floor first: move the chest and pelvis together.")
family("dip",
       "Check that the bars are stable and choose assistance if you cannot lower and rise smoothly.|Stop the descent before the shoulders roll forward or feel pinched.",
       "Start with a shallow, controlled range and build depth only as tolerated.|Use steps or a stable platform to mount and dismount; do not jump down with added weight.",
       "Dropping rapidly into the bottom: slow the lowering phase.|Shrugging or hanging passively: maintain active shoulder support.|Forcing extra depth: shorten the range rather than chasing an arbitrary elbow angle.")
family("machine_dip",
       "Set the seat so you can reach the handles without shrugging or twisting.|Keep your back against the pad and the selector fully seated before pressing.",
       "Check that the seat restraint, if fitted, is adjusted as the placard directs.|Start light and stop short of an uncomfortable shoulder stretch.",
       "Lifting off the seat: reduce the load and maintain contact.|Wrists folding back: align the handles with the forearms.|Snapping the elbows straight: finish the press under control.")
family("fly incline_fly",
       "Use lighter loads than a press; the long lever increases shoulder demand.|Keep a small, steady elbow bend and return the weights through a comfortable arc.",
       "Try the movement without weights to establish a comfortable bottom position.|Choose a floor version or shorter range if bench depth exceeds your control.",
       "Arms locked rigidly: retain a slight elbow bend.|Lowering far below the torso: shorten the arc before the shoulders roll forward.|Turning every repetition into a press: keep the elbow angle mostly constant.")
family("cable_fly",
       "Lock both pulley carriages and close attachment clips before stepping out.|Choose a stance that lets you resist the cable pull without losing balance.",
       "Begin with a small range and light stacks, especially in the stretched position.|Match the cable direction to the intended fly or press-around rather than forcing the shoulder.",
       "Leaning farther forward each rep: reduce the load and keep your trunk steady.|Opening the arms excessively behind the body: shorten the return.|Unequal pulleys or loads: check both sides before starting.")
family("pec_deck reverse_deck",
       "Adjust the seat and starting arms before sitting into the loaded position.|Use the direction and hand position shown for chest fly or reverse fly on the placard.",
       "Choose a start position that does not pull your shoulders behind a comfortable range.|Use light resistance until you can control both directions without lifting off the pad.",
       "Seat misaligned with handles: adjust before loading.|Forcing the arms farther back: reduce the start range.|Jerking the handles together or apart: move smoothly without banging the stack.")
family("pullover",
       "Hold the dumbbell securely with a grip appropriate to its design; avoid loose adjustable plates overhead.|Stop the overhead reach before the ribs flare or the shoulder feels strained.",
       "Practice with a light dumbbell and a stable bench before adding load.|Keep the torso supported and control how you bring the weight back to your lap.",
       "Turning the reach into a large back arch: brace the ribs down.|Repeatedly bending and straightening the elbows: maintain a small, steady bend.|Dropping into the stretch: lower slowly within your controlled range.")
family("row one_arm_row one_arm_cable_row seated_row",
       "Set your support or foot brace before taking cable or free-weight tension.|Keep the torso controlled; end the set if you cannot maintain your chosen back position.",
       "Use a lighter load to learn the row without torso momentum.|For single-arm rows, use a stable hand support if balance or rotation limits control.",
       "Heaving the chest upward: reduce weight and keep the torso steady.|Twisting toward the lifting arm: resist rotation through the trunk.|Shrugging every pull: move the elbow back without crowding the neck.")
family("supported_row high_row",
       "Adjust the pad so the handles are reachable while your chest stays supported.|Keep clear of lever arms and plates when getting on or off the machine.",
       "Test the reach with a light load so the shoulders are not forced forward.|Use a handle path you can complete without lifting your chest off the pad.",
       "Peeling the chest off the support: lower the load.|Pulling the neck forward: keep the head aligned with the torso.|Cutting the return short or dropping it: use a controlled, comfortable reach.")
family("inverted_row",
       "Use a bar or suspension system explicitly rated and secured for bodyweight rows.|Keep the body aligned and choose a foot position that lets you finish with control.",
       "Check the bar cannot roll, lift out or slide before leaning underneath it.|Start more upright, then increase the body angle gradually.",
       "Hips dropping: shorten the set or make the body angle easier.|Chin jutting toward the bar: lead with the chest.|Bouncing from the bottom: keep tension and reverse smoothly.")
family("pulldown one_arm_pulldown",
       "Secure the thigh restraint and select a load that will not lift you out of the seat.|Pull in front of the head through a comfortable path; do not force the bar behind the neck.",
       "Choose a grip width that lets the elbows move freely without wrist strain.|Use a lighter load if you need to lean or jerk to start the pull.",
       "Leaning back into a row: keep a small, consistent torso angle.|Pulling with bent wrists: align hands with forearms.|Letting the stack drag the shoulders upward suddenly: control the overhead return.")
family("pullup chinup",
       "Check the bar and use secure steps to reach it; clear the landing area.|Choose a band or assisted machine only if you understand its mounting and release procedure.",
       "Start with a grip and range your shoulders tolerate without pinching.|Build controlled assisted repetitions before adding weight.",
       "Kipping to finish strict reps: use more assistance or stop earlier.|Craning the neck over the bar: lift with the arms and back.|Dropping abruptly into the hang: control the descent.")
family("straight_arm_pull",
       "Lock the pulley above you and secure the attachment before taking tension.|Keep a modest elbow bend and avoid forcing the shoulders into an extreme overhead stretch.",
       "Start light enough that the trunk can remain steady.|Adjust your distance from the tower so the cable runs freely without rubbing.",
       "Bending the elbows into a pushdown: keep the elbow angle mostly fixed.|Rocking the torso: brace and reduce the load.|Overarching at the top: keep ribs and pelvis controlled.")
family("overhead_press seated_press machine_shoulder_press",
       "Use a pressing path that clears your face without excessive back bending.|For a guided machine, adjust the seat and starting handles before loading; for free weights, plan a controlled return.",
       "Choose a grip and elbow position that allow a comfortable overhead reach.|If seated, lock the back pad; if standing, brace the trunk and maintain stable footing.",
       "Leaning back to turn the lift into a chest press: reduce the load.|Forcing the elbows far behind the torso: choose a comfortable start position.|Rushing the lowering phase: control the return instead of dropping the weight.")
family("lateral_raise seated_raise fdl_raise front_raise",
       "Use a light load and a comfortable shoulder arc; stop before a painful pinch.|Keep wrists controlled and give the weights or cables space to move clear of others.",
       "Raise only as far as you can without shrugging or leaning.|For front / lateral / rear sequences, choose a load suitable for the weakest phase.",
       "Swinging with the legs or trunk: slow down and reduce weight.|Lifting shoulders toward the ears: keep the neck relaxed.|Pouring the hands sharply downward: use a neutral, comfortable hand position.")
family("rear_fly face_pull pull_apart y_raise",
       "Use light resistance and a shoulder position you can control without pinching.|For bands and cables, inspect the anchor and attachment before pulling toward your face.",
       "Keep enough distance from the anchor for smooth resistance without overstretching the band.|If bent over, support the torso when needed to reduce balance demands.",
       "Throwing the arms backward: reduce resistance and slow the return.|Arching the back to gain range: brace the trunk.|Shrugging and jutting the head forward: keep the neck long and relaxed.")
family("external_rotation internal_rotation",
       "Use very light resistance; the goal is controlled shoulder rotation, not maximal loading.|Keep the upper arm supported or in the prescribed position, and stop before pain.",
       "Use only the rotation range you can achieve without moving the whole shoulder forward.|Get individual guidance before loading an injured or unstable shoulder.",
       "Rotating the torso instead of the shoulder: keep the trunk still.|Letting the elbow drift: support the upper arm.|Forcing the end range: shorten the arc and reduce resistance.")
family("upright_row",
       "Use a comfortable grip and stop the lift before the shoulder feels compressed.|Keep the load close without wrenching the wrists into an awkward bend.",
       "Begin with a low lift height and increase range only while comfortable.|Choose a cable, dumbbell or wider grip if a fixed bar position does not suit your shoulders.",
       "Pulling elbows as high as possible: stop at a comfortable height.|Heaving the hips: lower the load.|Bending wrists to lift farther: keep the hands aligned with the forearms.")
family("shrug seated_shrug",
       "Choose a load you can hold and lower safely without grip slipping.|Elevate and lower the shoulder blades without rolling the shoulders in large circles.",
       "Keep the neck in a comfortable neutral position.|Use smaller loads or shorter sets if grip or posture fails before the shoulders.",
       "Rolling shoulders: move up and down smoothly.|Jutting the chin forward: keep the head over the torso.|Bouncing heavy weights: use a controlled pause and lowering phase.")
family("curl supported_curl hammer_curl zottman reverse_curl",
       "Keep the wrist aligned with the forearm and avoid forcing a painful grip angle.|For supported curls, fit the pad to your upper arm and avoid snapping into full elbow extension.",
       "Use an angled or neutral grip if a straight bar strains the wrist.|Build load gradually while preserving a controlled lowering phase.",
       "Swinging the torso: reduce the load.|Driving elbows forward to finish: keep the upper arms in the intended position.|Dropping rapidly into the bottom: slow down before the elbow reaches its limit.")
family("wrist_curl reverse_wrist wrist_rotation",
       "Support the forearm and use a small load appropriate to wrist or forearm rotation.|Keep the movement within a comfortable range without forcing the hand into an end position.",
       "Increase small muscle-group loads in small increments.|For pronation or supination, start with a short lever and light dumbbell held securely.",
       "Moving the whole elbow or shoulder: keep the forearm supported.|Using a heavy, long lever: reduce load or shorten the lever.|Bouncing at end range: turn or bend the wrist slowly.")
family("pushdown overhead_extension skullcrusher machine_triceps kickback",
       "Choose a load that lets you keep the upper arm controlled throughout elbow extension.|Keep overhead or lying weights secure and choose a clear, controlled way to end the set.",
       "Use a grip and elbow path that do not cause joint discomfort.|For overhead work, shorten the reach rather than forcing shoulder range or arching the back.",
       "Swinging the upper arms: reduce the load and hold their position.|Folding the wrists: keep hands aligned with forearms.|Snapping the elbow straight or dropping into the stretch: move through a controlled range.")
family("squat front_squat machine_squat deep_squat",
       "Use safeties and a load you can control; test any guided-machine release before the set.|Keep the feet planted and choose a depth that preserves control of the trunk and pelvis.",
       "Warm up the squat pattern and increase resistance in stages.|Use a stance that allows the knees to track in the same general direction as the toes.",
       "Heels lifting or weight tipping forward: adjust stance, depth or load.|Knees collapsing inward: slow down and maintain foot pressure.|Forcing depth after the pelvis or back loses position: shorten the range.")
family("sissy_squat",
       "Use a fixed support or purpose-built machine and start with bodyweight.|Keep the knee bend within a comfortable range and avoid a fast drop backward.",
       "Build range gradually rather than adding load immediately.|Choose a supported squat variation if you cannot control the knee-dominant lean.",
       "Dropping into the bottom: lower slowly.|Using an unstable support: choose a fixed handhold.|Pushing through knee pain: stop and change the movement.")
family("leg_press",
       "Set the seat or backrest so the pelvis stays supported throughout the return.|Identify and test the machine's stop or re-rack mechanism before loading.",
       "Choose foot placement and depth that allow the whole foot to stay planted.|Add resistance only when you can reverse the sled or seat without bouncing.",
       "Lowering until the lower back rounds off the pad: reduce depth.|Snapping knees into hyperextension: extend under control.|Lifting the heels or letting knees collapse: adjust placement and lighten the load.")
family("lunge lateral_lunge smith_lunge stepup",
       "Choose stable footing and a step height or split stance you can control.|For step-ups, use a platform rated for stepping; do not stand on an adjustable bench unless its manufacturer permits it.",
       "Start with bodyweight or hand support if balance limits the movement.|Use a stride and range that allow the knee and foot to stay aligned without pain.",
       "Pushing off heavily with the trailing leg on step-ups: use a lower step and control the working leg.|Knee collapsing inward: slow the movement and stabilize the foot.|Rushing the descent: lower under control instead of dropping.")
family("deadlift rack_pull clean",
       "Clear the lifting area, secure plate-loaded bars and keep the load close to your body.|For cleans, learn the pull and front-rack catch with a qualified coach before loading; do not improvise a catch with heavy weight.",
       "Practice the setup and progressively warm up before working sets.|Use a grip you can maintain and stop before the trunk or bar path becomes uncontrolled.",
       "Jerking the load off the floor: take tension and drive smoothly.|Letting the load drift away: keep it close to the legs.|Leaning backward at lockout: finish tall without forcing a backbend.")
family("rdl single_rdl good_morning pullthrough",
       "Hinge through the hips with controlled knee bend; stop the descent before you lose trunk position.|Keep the load close or the bar securely supported, and keep a clear path to set it down.",
       "Use hamstring tension and posture, not reaching the floor, to choose depth.|Use hand support for single-leg hinges and very light loads when learning good mornings.",
       "Reaching lower by rounding the back: shorten the descent.|Turning the hinge into a deep squat: move the hips back with modest knee bend.|Thrusting the ribs up at lockout: stand tall with the pelvis controlled.")
family("hip_thrust",
       "Brace the bench against movement and pad the load across the hip crease, not the abdomen.|For Smith or barbell versions, plan the entry, safeties and exit before loading.",
       "Start with a bodyweight bridge to find comfortable foot and upper-back placement.|Use a range you can complete without forcing the lower back into extension.",
       "Finishing by arching the lower back: keep the ribs and pelvis controlled.|Bench sliding: stop and secure or reposition the support.|Feet too far away or too close for control: adjust placement with a light load.")
family("back_extension reverse_hyper superman",
       "Use a comfortable range and avoid throwing the trunk or legs into a large backbend.|For extension benches, fit the hip pad and foot restraints before adding resistance.",
       "Start with bodyweight and a short arc; add resistance only after control is consistent.|For hip-dominant extensions, let the hips hinge while the back position stays steady.",
       "Swinging through the bottom: slow down.|Lifting far past a straight body line: stop at a controlled finish.|Holding the breath through a long set: breathe while keeping the trunk supported.")
family("leg_curl standing_leg_curl glute_ham",
       "For machine curls, align the working knee with the pivot and fit the roller above the heel as the placard directs.|For glute-ham raises, secure the feet and use assistance before attempting an unsupported full range.",
       "Start with a load or lever length that allows the hips to remain controlled.|Check pad pressure and shorten the range if the knee or hamstring feels strained.",
       "Lifting the hips to curl farther: reduce load or range.|Ankle roller pressing the Achilles tendon: reposition the pad.|Dropping the lowering phase: control the return instead of letting the lever pull you down.")
family("leg_extension",
       "Align the knee joint with the machine pivot; fit the roller on the lower shin above the ankle.|Use a controlled range without kicking the lever or forcing a painful knee position.",
       "Adjust the back pad so the thighs are supported and the knees line up with the hinge.|Increase load gradually while keeping the hips on the seat.",
       "Knee positioned ahead of or behind the pivot: refit the seat.|Kicking the pad with momentum: lower resistance and slow down.|Lifting the hips off the seat: keep the torso and pelvis supported.")
family("abduction adduction cable_hip",
       "Adjust the starting width or ankle cuff before loading the hips.|For standing cable work, secure a fixed handhold and keep the cable clear of the stance foot.",
       "Use a comfortable hip range without forcing the knees apart or together.|Start light so the pelvis can stay level and the trunk does not lean.",
       "Bouncing at the widest position: reduce range and move slowly.|Rotating the whole pelvis: keep the hips facing the intended direction.|Using an extreme start stretch: adjust the machine or stance to a comfortable position.")
family("standing_calf supported_calf seated_calf",
       "Place the ball of the foot securely on the platform, with enough contact that it cannot slip.|For loaded calf machines, test the release and re-rack with a light load before starting.",
       "Use a gentle heel descent; build range and load gradually rather than bouncing into the Achilles tendon.|Choose hand support or a guided machine if balance limits control.",
       "Bouncing at the bottom: pause and reverse smoothly.|Rolling onto the outer edge of the foot: maintain even forefoot pressure.|Changing knee angle every rep: keep the chosen straight or bent-knee position consistent.")
family("crunch situp reverse_crunch bicycle v_up hanging_raise leg_raise",
       "Use a range you can control without neck strain or an uncomfortable pull in the lower back.|For hanging versions, check the grip and overhead support before lifting the legs.",
       "Start with bent knees or a shorter lever if you cannot control the pelvis.|Exhale through the effort and end the set when trunk control is lost.",
       "Pulling on the head: keep the hands light and move through the trunk.|Swinging the legs: slow down or use a supported version.|Arching the lower back to reach farther: shorten the range or lever.")
family("plank side_plank hollow rollout pallof",
       "Hold only while you can keep the trunk controlled and continue breathing.|For rollouts, use a nonrolling start position until ready, then limit the reach so you can return without the back sagging.",
       "Shorten the lever, elevate the hands or use shorter holds before adding difficulty.|Check that the floor, wheel, barbell or cable anchor is stable and clear of slipping hazards.",
       "Hips sagging or ribs flaring: shorten the hold or reach.|Holding the breath: breathe quietly while bracing.|Chasing time after posture fails: stop the set and use an easier version.")
family("jump_squat",
       "Use a clear, nonslip landing area and begin with low bodyweight jumps.|Land softly through the hips, knees and ankles; stop before fatigue changes landing control.",
       "Practice quiet landings before increasing jump height or speed.|Use a controlled squat instead if repeated landings cause joint pain.",
       "Landing with rigid knees: absorb the landing through a comfortable bend.|Knees collapsing inward: reduce jump height and control alignment.|Rushing consecutive jumps: reset balance between repetitions.")
family("pass_through child_cobra child_reach leg_swing lunge_arch side_lunge_stretch hamstring_stretch world_stretch",
       "Move gently through a comfortable range; stretching should not cause sharp pain or numbness.|Use a stable support when balance is required and keep the area clear.",
       "Warm up with easy movement before deeper positions.|Increase range gradually without forcing the shoulder, hip or spine into an end position.",
       "Bouncing into a stretch: move slowly and ease out under control.|Holding the breath: maintain relaxed breathing.|Compensating with a large back arch or twist: reduce the range and isolate the intended region.")
family("foam_roll",
       "Apply gentle pressure to muscle tissue; avoid direct rolling on joints, bony areas or the neck.|Do not roll over an acute injury, unexplained swelling or areas where a clinician has advised against pressure.",
       "Support enough bodyweight with the hands or feet to control pressure.|Use short, comfortable passes and stop for sharp pain, tingling or numbness.",
       "Putting full bodyweight onto a painful spot: reduce pressure.|Rolling directly on the lower spine: choose adjacent muscle regions and suitable support.|Treating severe pain as a sign of benefit: stop and seek appropriate guidance.")
family("carry",
       "Choose weights you can pick up, carry and set down with control.|Clear a level route and keep fingers away from racks when returning the weights.",
       "Start with short distances or holds while maintaining normal breathing.|For one-sided carries, use a lighter load that does not pull your trunk sideways.",
       "Leaning toward or away from the weight: lower the load.|Rushing with short, unstable steps: walk steadily.|Shrugging or letting the grip open: end the set and place the weights down safely.")
family("chop rotation",
       "Use light resistance and rotate only through a comfortable, controlled range.|For cables, lock the pulley and keep the attachment away from other people.",
       "Allow an appropriate foot pivot for standing chops instead of twisting a fixed knee.|On a seated rotation machine, use its prescribed restraints and avoid forcing extra spinal rotation.",
       "Yanking with the arms: control the trunk and cable path.|Twisting farther by momentum: slow down and reduce range.|Letting the knees or pelvis collapse: stabilize the lower body or adjust foot position.")

# Individual equipment and movement differences remain explicit.
PROFILES["deadlift"] = {**PROFILES["deadlift"], "safetyTips": [
    "Clear the lifting area and secure plate-loaded bars before taking your grip.",
    "Keep the load close and stop if you cannot maintain a controlled trunk position."]}
PROFILES["rack_pull"] = {**PROFILES["rack_pull"], "safetyTips": [
    "Set both rack supports to equal heights and check that they are rated for the loaded bar.",
    "Keep hands clear of the supports when lowering; return the bar evenly to both sides."]}
PROFILES["seated_calf"] = {**PROFILES["seated_calf"], "safetyTips": [
    "Fit the thigh pad above the knees and keep the forefoot securely on the platform.",
    "Learn the release and re-rack procedure with a light load before starting."]}
PROFILES["hanging_raise"] = {**PROFILES["hanging_raise"], "safetyTips": [
    "Check the overhead support and use a secure, comfortable grip.",
    "Raise and lower the legs without swinging; use a supported version if grip fails before the core."]}
PROFILES["leg_raise"] = {**PROFILES["leg_raise"], "mistakes": [
    "Lowering the legs beyond pelvic control: bend the knees or stop higher.",
    "Arching the lower back: shorten the lever and keep the trunk controlled.",
    "Swinging the legs: move slowly and reset between repetitions."]}
PROFILES["rollout"] = {**PROFILES["rollout"], "safetyTips": [
    "Use a clear, nonslip surface and a stable wheel or evenly loaded barbell that rolls smoothly.",
    "Limit the reach to a distance you can reverse without sagging or collapsing through the shoulders."]}
PROFILES["lunge"] = {**PROFILES["lunge"], "mistakes": [
    "Using a stride too long or short to control: adjust the stance with bodyweight first.",
    "Knee collapsing inward: slow the movement and stabilize the foot.",
    "Dropping rapidly or losing balance on the return: use hand support and a shorter range."]}
PROFILES["stepup"] = {**PROFILES["stepup"], "mistakes": [
    "Pushing off heavily with the trailing leg: use a lower step and control the working leg.",
    "Placing only the forefoot on the step: put the whole working foot securely on the platform.",
    "Jumping or dropping off the step: lower under control and reset balance."]}
PROFILES["lateral_lunge"] = {**PROFILES["lateral_lunge"], "mistakes": [
    "Taking an excessively wide step: choose a width you can reverse without a painful groin stretch.",
    "Bent knee collapsing inward: keep it tracking with the planted foot.",
    "Rounding the back to reach lower: reduce depth and hinge through the hip."]}
PROFILES["hanging_raise"] = {**PROFILES["hanging_raise"], "mistakes": [
    "Swinging the legs and torso: pause and use a controlled lift.",
    "Arching the back at the bottom: shorten the lowering range and control the pelvis.",
    "Continuing as the grip opens: end the set and dismount safely."]}
PROFILES["deep_squat"] = {**PROFILES["deep_squat"], "safetyTips": [
    "Use a clear, nonslip floor and a stance that keeps both feet planted.",
    "Choose a comfortable depth without forcing the knees, hips or back into an end position."]}
family("dumbbell_leg_curl",
       "Do not improvise a heavy or unstable dumbbell hold between the feet; ask a trainer to verify the setup or choose a machine curl.|Keep the body supported and stop before the weight can slip as the legs fatigue.",
       "Use a very light rehearsal and a clear area before considering added resistance.|Choose a seated or lying leg-curl machine if you cannot secure and release the dumbbell reliably.",
       "Using a weight that shifts or slips: stop and choose a more secure exercise setup.|Lifting the hips to curl farther: reduce the load and keep the pelvis supported.|Lowering too quickly to retain control: shorten the set and use a controlled return.")

# Exact catalog aliases not already present in the program muscle-map vocabulary.
CATALOG_ALIASES = {
    "Dumbbell Hamstring Curl": "dumbbell_leg_curl",
    "Concentration curl": "supported_curl", "Overhead cable triceps extension": "overhead_extension",
    "Cable rope pushdown": "pushdown", "Machine triceps extension": "machine_triceps",
    "Cable pull-through": "pullthrough", "Reverse pec deck": "reverse_deck",
    "Cable face pull": "face_pull", "Seated dumbbell shoulder press": "seated_press",
    "Seated Chest Press": "machine_press", "Smith Machine Bench Press": "machine_press",
    "Seated Leg Press": "leg_press", "Dumbbell Incline Press": "incline", "Cable curl": "curl",
    "Dumbbell preacher curl": "supported_curl", "Behind-body cable curl": "supported_curl",
    "Assisted chin-up": "chinup", "Straight-bar cable pushdown": "pushdown",
    "Single-arm cable pushdown": "pushdown", "Close-grip Smith bench press": "close_press",
    "Lying EZ-bar triceps extension": "skullcrusher", "Seated overhead dumbbell extension": "overhead_extension",
    "Assisted dip": "dip", "Cable triceps kickback": "kickback", "EZ-bar reverse curl": "reverse_curl",
    "Seated dumbbell wrist curl": "wrist_curl", "Seated dumbbell reverse wrist curl": "reverse_wrist",
    "Farmer carry": "carry", "Heavy dumbbell hold": "carry", "Cable wrist curl": "wrist_curl",
    "Cable reverse wrist curl": "reverse_wrist", "Dumbbell forearm pronation": "wrist_rotation",
    "Dumbbell forearm supination": "wrist_rotation", "Smith squat": "machine_squat",
    "Dumbbell Romanian deadlift": "rdl", "Dumbbell reverse lunge": "lunge",
    "Smith hip thrust": "hip_thrust", "Cable glute kickback": "cable_hip",
    "Hip adduction machine": "adduction", "Hip abduction machine": "abduction",
    "Standing cable hip abduction": "cable_hip", "Standing calf raise machine": "supported_calf",
    "Seated calf raise machine": "seated_calf", "Supported single-leg dumbbell calf raise": "standing_calf",
    "Single-leg seated dumbbell calf raise": "seated_calf", "Single-leg leg press calf raise": "supported_calf",
    "Supported bodyweight calf raise": "standing_calf", "Seated lumbar extension machine": "back_extension",
    "45-degree back extension": "back_extension", "Smith Romanian deadlift": "rdl",
    "Trap-bar deadlift": "deadlift", "Suitcase carry": "carry",
    "Supported single-leg dumbbell Romanian deadlift": "single_rdl", "Machine row": "supported_row",
    "Assisted pull-up": "pullup", "Incline dumbbell Y-raise": "y_raise",
    "Cable front raise": "front_raise", "Flat dumbbell bench press": "bench",
    "Smith incline bench press": "machine_incline", "Standing cable chest fly": "cable_fly",
    "Incline machine chest press": "machine_incline", "Low-to-high cable fly": "incline_fly",
    "Seated abdominal crunch machine": "crunch", "Kneeling cable crunch": "crunch",
    "Standing cable Pallof press": "pallof", "Standing cable crunch": "crunch", "Seated cable crunch": "crunch",
    "High-to-low cable chop": "chop", "Low-to-high cable lift": "chop", "Cable side bend": "rotation",
    "Torso rotation machine": "rotation", "Cable Pallof hold": "pallof",
    "Machine lateral raise": "lateral_raise",
}
