"""Authored setup, movement and breathing sequences for the program exercise pages."""
RECIPES = {}


def recipe(ids, setup, steps, breathing, movement, difficulty="Beginner"):
    value = dict(setup=setup.split("|"), steps=steps.split("|"), breathing=breathing,
                 movement=movement, difficulty=difficulty)
    for id in ids.split():
        assert id not in RECIPES, id
        RECIPES[id] = value


recipe("bench incline decline close_press machine_press machine_incline",
       "Choose the prescribed flat, incline or decline support and lock every adjustment.|Position the upper back on the support, feet stable, with a grip that keeps wrists over forearms.|Set rack or machine safeties where provided and rehearse the unrack and return with a light load.",
       "Bring the handles or weights into a balanced starting position.|Lower through a comfortable pressing path while keeping the upper back supported.|Press away from the chest without throwing the shoulders forward or snapping the elbows backward.|Return slowly, repeat with the same body position, then re-rack securely.",
       "Breathe out through the press and in during the controlled return; keep the trunk supported.", "Push")
recipe("pushup close_pushup",
       "Use a clear, nonslip floor or a fixed elevated hand support.|Place hands at a comfortable width; a closer hand position emphasizes elbow extension.|Step the feet back and brace the trunk so the head, torso and hips move together.",
       "Bend the elbows and lower the chest toward the support.|Keep a steady body line and a comfortable diagonal elbow path.|Press the support away until the arms are straight without forcing the joints.|Repeat only while you can keep the trunk from sagging; use a higher support if needed.",
       "Inhale while lowering and exhale while pressing away.", "Push")
recipe("dip machine_dip",
       "Use stable dip bars, or adjust the seat and handles on the prescribed dip machine.|Choose assistance or a light machine load that allows a controlled descent.|Use the intended steps and handles to mount, and establish active shoulder support.",
       "Bend the elbows and lower only through a comfortable shoulder range.|Keep the neck relaxed and the wrists controlled.|Extend the elbows to raise the body or press the machine handles down without bouncing.|Control the return and use the designated steps or re-rack procedure to finish.",
       "Inhale on the lowering phase and exhale through elbow extension.", "Push", "Intermediate")
recipe("fly incline_fly pec_deck cable_fly",
       "Set the bench, machine arms or cable height for the specified fly direction.|Use a light load, a small elbow bend and a supported torso or stable standing stance.|Choose a starting reach that does not force the shoulders far behind the body.",
       "Move the arms inward in an arc while keeping the elbow angle mostly steady.|Bring the handles or weights toward the midline without striking them together.|Open slowly until you reach a comfortable chest stretch with shoulder control.|Repeat without turning the movement into a large press or rocking the torso.",
       "Exhale as the arms come together and inhale as they open.", "Push")
recipe("pullover",
       "Use a stable, locked bench and a light dumbbell with a secure grip.|Support the torso on the bench with feet stable and the weight balanced over the chest.|Set a modest elbow bend and brace the ribs before moving overhead.",
       "Lower the weight in an arc toward and behind the head.|Stop the reach before the ribs flare or the shoulders feel strained.|Bring the weight back over the chest using shoulder movement, keeping the elbow bend steady.|Finish with a controlled return to the lap or another rehearsed safe position.",
       "Inhale through the controlled reach and exhale as the weight returns over the chest.", "Pull", "Intermediate")
recipe("row one_arm_row one_arm_cable_row seated_row supported_row high_row",
       "Set the prescribed seat, chest support, hand support or hip-hinge position before taking the load.|Take an even grip and reach the arms without losing trunk control.|For a single-arm row, stabilize the pelvis and choose a load you can move without twisting.",
       "Draw the elbow or elbows back along the intended row path.|Move the shoulder blades with control rather than shrugging the neck.|Pause at a comfortable finish without lifting off a chest pad or leaning farther backward.|Reach forward slowly while keeping the same supported or braced torso position.",
       "Exhale while rowing and inhale through the controlled reach.", "Pull")
recipe("inverted_row",
       "Use a bar or suspension anchor rated and fixed for bodyweight rows.|Position yourself beneath it with a secure grip and heels or feet on nonslip flooring.|Choose a body angle you can hold as a steady line; a more upright angle is easier.",
       "Pull the chest toward the bar or handles by bending the elbows.|Keep the hips in line with the torso and the head comfortably aligned.|Pause without jutting the chin toward the bar.|Lower the body slowly and repeat without bouncing or letting the hips drop.",
       "Exhale on the pull and inhale as you lower.", "Pull")
recipe("pulldown one_arm_pulldown",
       "Set the seat, thigh restraint and prescribed handle; for a one-arm cable version, use a stable stance or seat.|Choose a comfortable grip and enough resistance to move smoothly without being pulled out of position.|Begin with an overhead reach you can control and a small, steady torso angle.",
       "Draw the elbows down toward the sides of the torso.|Bring the attachment toward the upper-chest region without pulling behind the neck.|Pause without leaning farther back to finish.|Let the arms reach overhead slowly, keeping the torso and pelvis controlled.",
       "Exhale while pulling down and inhale during the upward return.", "Pull")
recipe("pullup chinup",
       "Use a secure pull-up bar with a clear mounting and landing area.|Choose an overhand grip for a pull-up or an underhand grip for a chin-up; use the width prescribed.|Mount using the intended steps and establish a controlled hang with the trunk braced.",
       "Bend the elbows and draw them toward the sides as the body rises.|Lift through a range you can control without kicking or craning the neck.|Pause near the top while maintaining shoulder support.|Lower slowly to a comfortable hang, then dismount using the intended support.",
       "Exhale through the pull and inhale during the controlled lowering phase.", "Pull", "Intermediate")
recipe("straight_arm_pull",
       "Lock a high pulley and secure the prescribed rope, bar or handles.|Stand far enough away for the cable to move freely and use a stable stance.|Reach the arms forward or overhead with a slight elbow bend and the ribs controlled.",
       "Move the upper arms down and back toward the sides or thighs.|Keep the elbow angle mostly unchanged instead of curling or pushing through it.|Pause without driving the hips forward or leaning backward.|Return to the controlled overhead reach without letting the cable pull the back into an arch.",
       "Exhale while drawing the arms down and inhale during the return.", "Pull")
recipe("overhead_press seated_press machine_shoulder_press",
       "Choose the prescribed standing, supported seated or guided-machine position.|Position the load near shoulder level with wrists and forearms aligned.|Lock any bench or machine adjustments, stabilize the feet, and clear the overhead path.",
       "Press upward through a comfortable shoulder path while keeping the trunk controlled.|Move the load clear of the face without adding a large back arch.|Finish overhead without forcing the elbows or shrugging excessively.|Lower slowly toward shoulder level and use a controlled re-rack or return to finish.",
       "Exhale through the press and inhale while lowering; avoid prolonged breath-holding.", "Push", "Intermediate")
recipe("lateral_raise seated_raise front_raise",
       "Choose light dumbbells or the prescribed cable attachment and height.|Stand or sit with the torso stable, elbows softly bent and wrists comfortable.|Set the intended direction: outward for a lateral raise, forward for a front raise.",
       "Raise the arms through a comfortable arc without swinging the trunk.|Stop before the shoulders shrug or feel pinched.|Pause briefly with control rather than lifting as high as possible.|Lower slowly and repeat with the same elbow bend and body position.",
       "Exhale as the arms rise and inhale as they lower.", "Push")
recipe("fdl_raise",
       "Interpret the program's FDL label as a front, lateral and rear-delt raise sequence; confirm the intended sequence with its source.|Use a light dumbbell pair and a stable seat.|Choose a load you can control through the weakest of the three raise directions.",
       "Perform the prescribed front-raise phase with the trunk steady.|Perform the lateral-raise phase in a comfortable outward arc.|For the rear-delt phase, use a controlled hip hinge or suitable chest support and open the arms outward.|Lower slowly in each phase; follow the program's count rather than adding unprescribed repetitions.",
       "Exhale through each raise and inhale during each controlled lowering phase.", "Push")
recipe("rear_fly reverse_deck pull_apart face_pull y_raise",
       "Choose the prescribed band, cable, dumbbell or reverse-fly machine setup.|Establish a supported chest position, controlled hinge or stable standing stance.|Use light resistance and begin with the arms in a comfortable forward reach.",
       "Open or draw the arms backward through the prescribed shoulder path.|For face pulls, draw the rope toward face level with controlled shoulder rotation; for pull-aparts, widen the hands.|Keep the trunk steady and the neck relaxed as the shoulder blades move.|Return the arms slowly without throwing the load or letting the band snap back.",
       "Exhale on the opening or pulling phase and inhale as the arms return.", "Pull")
recipe("external_rotation internal_rotation",
       "Use a very light band or cable and establish the prescribed upper-arm position.|Support the elbow or keep it steadily beside the torso when that is the prescribed variation.|Keep the wrist aligned and the shoulder relaxed without letting the whole torso rotate.",
       "Rotate the upper arm gently in the named direction while holding the elbow position.|For external rotation, move the forearm away from the body; for internal rotation, bring it inward.|Stop before the shoulder rolls forward or the movement becomes uncomfortable.|Reverse slowly without rotating the trunk or changing the upper-arm position.",
       "Breathe steadily; exhale through the resisted rotation.", "Pull")
recipe("upright_row",
       "Choose the prescribed bar, cable or dumbbell grip that allows comfortable wrists.|Stand with feet stable and the load close in front of the thighs.|Use a light load and decide on a comfortable lift height before starting.",
       "Bend the elbows and guide them outward as the load rises close to the torso.|Stop before the shoulder feels compressed or the wrists fold awkwardly.|Keep the torso steady rather than heaving the hips.|Lower slowly along the same close path and repeat without forcing additional height.",
       "Exhale on the lift and inhale on the return.", "Pull")
recipe("shrug seated_shrug",
       "Choose the prescribed standing or seated position with weights balanced at the sides or in front.|Take a secure grip and keep the head comfortably above the torso.|Select a load you can hold and set down without losing posture.",
       "Elevate the shoulder blades in a smooth upward movement.|Pause without jutting the chin forward.|Lower the shoulders slowly rather than rolling them in circles.|Repeat while grip and neck position remain controlled, then return the weights safely.",
       "Exhale during elevation and inhale while lowering.", "Pull")
recipe("curl supported_curl hammer_curl zottman reverse_curl",
       "Choose the prescribed grip and a load that does not require torso momentum.|Stand steadily or adjust the seat and upper-arm support for the named variation.|Begin with wrists aligned and the upper arms in the intended position.",
       "Bend the elbows to bring the weight toward the shoulders without swinging.|Use palms up for a standard curl, thumbs up for a hammer curl, or the prescribed pronated grip.|For Zottman curls, rotate to a palms-down grip for the controlled lowering phase.|Lower slowly to a comfortable elbow extension while holding the upper-arm position.",
       "Exhale as the elbows bend and inhale while lowering.", "Pull")
recipe("wrist_curl reverse_wrist wrist_rotation",
       "Use a light load and support the forearms on a stable surface or the thighs.|Let the wrist move beyond the support edge without moving the elbow.|Choose palms up for wrist curls, palms down for reverse wrist curls, or the prescribed rotation grip.",
       "Move slowly through the named wrist or forearm action.|For wrist curls, flex the wrist; for reverse wrist curls, lift the knuckles through extension.|Keep the elbow and shoulder still and stop before a forced end position.|Return under control using the same small, comfortable range.",
       "Breathe continuously; exhale through the resisted movement.", "Pull")
recipe("pushdown overhead_extension skullcrusher machine_triceps kickback",
       "Choose the named cable, free-weight or supported machine setup.|Place the upper arms in the required position: beside the torso, overhead, or supported for a lying extension.|Keep wrists controlled, use a modest load and plan how to return the weight safely.",
       "Extend through the elbows while keeping the upper arms largely still.|Stop short of snapping the elbows backward or forcing an uncomfortable finish.|Bend the elbows slowly to return through a comfortable stretch.|Keep the trunk and shoulders steady throughout, then re-rack or lower the load safely.",
       "Exhale through elbow extension and inhale during the controlled bend.", "Push")
recipe("squat front_squat machine_squat deep_squat sissy_squat",
       "Choose the named bodyweight, front-loaded, back-loaded or guided-machine setup.|Set feet in a comfortable stance and establish stable whole-foot pressure.|Fit the prescribed support and test safeties or machine catches before adding resistance.",
       "Bend the knees and hips together while keeping the trunk controlled.|Lower only as far as the feet and pelvis remain stable and the movement is comfortable.|Press through the whole foot to stand without letting the knees collapse inward.|Finish tall without snapping the knees backward, then repeat or re-rack safely.",
       "Inhale and establish support before lowering; exhale through the upward effort.", "Legs", "Intermediate")
recipe("leg_press",
       "Fit the seat and backrest so the pelvis stays supported throughout the intended depth.|Place the whole foot securely on the platform and locate the machine's release and re-rack controls.|Start with a light selection or balanced plate load and test the catch mechanism.",
       "Press the platform or seat away by extending through the knees and hips.|Keep the feet planted and finish without forcing knee hyperextension.|Bend the knees and hips slowly until just before the pelvis rolls off the pad.|Repeat under control and re-engage the appropriate catches before leaving the machine.",
       "Exhale through the press and inhale on the controlled return.", "Legs")
recipe("lunge lateral_lunge smith_lunge stepup",
       "Choose the named stepping direction, split stance or approved step platform.|Start with bodyweight or a light load and use a stable hand support when needed.|Set a stride or step height that permits controlled foot, knee and pelvic alignment.",
       "Transfer weight onto the working leg and bend through the hip and knee.|For lunges, lower through the prescribed forward, backward or sideways stance; for step-ups, place the whole foot on the step.|Drive through the working foot to rise without a sudden push from the trailing leg.|Return slowly to the start and change sides according to the program.",
       "Inhale while lowering and exhale while driving upward.", "Legs")
recipe("deadlift rack_pull",
       "Use the prescribed bar and a clear, level lifting area; secure plate-loaded equipment.|For a floor pull, position the load close to the midfoot; for rack pulls, set equal, rated supports.|Hinge and bend the knees enough to take a secure grip with a controlled trunk position.",
       "Take tension without jerking and press through the feet.|Extend through the hips and knees while keeping the load close.|Stand tall without leaning backward to finish.|Hinge and bend the knees to return the load evenly to the floor or both rack supports.",
       "Establish trunk support before each repetition and breathe between efforts; learn heavy-load bracing with qualified instruction.", "Legs", "Intermediate")
recipe("rdl single_rdl good_morning pullthrough",
       "Choose the named load position and establish stable feet with a modest knee bend.|Keep the load close to the body, or support the bar securely across the shoulders for good mornings.|For single-leg work, use a fixed hand support if needed and keep the pelvis controlled.",
       "Move the hips backward while keeping the trunk supported.|Lower only until hamstring tension or posture sets a comfortable limit; reaching the floor is not required.|Drive through the working foot or feet and extend the hips to stand.|Finish without pushing the ribs up into a backbend, then repeat with the same hinge pattern.",
       "Inhale and establish support before the hinge; exhale as you return upright.", "Legs", "Intermediate")
recipe("hip_thrust",
       "Use a stable bench or support permitted for hip thrusts and prevent it from sliding.|Set the upper back against the intended support, feet planted and knees comfortably bent.|Pad any bar or dumbbell across the hip crease and rehearse entry, safeties and exit with a light load.",
       "Drive through the feet to raise the pelvis through hip extension.|Keep the ribs and pelvis controlled rather than gaining height by arching the lower back.|Pause near a straight hip position without forcing extra extension.|Lower the hips slowly and keep the load balanced throughout the set.",
       "Exhale as the hips rise and inhale as they lower.", "Legs")
recipe("back_extension reverse_hyper superman",
       "Choose the named supported bench, reverse-hyper machine or prone floor position.|Fit any hip pad and foot restraints before taking bodyweight or external load.|Begin with a short range and a controlled trunk position.",
       "For hip-dominant back extensions, hinge through the hips while keeping the back position steady.|For reverse hypers, raise the legs with the torso supported; for Superman, gently raise the prescribed limbs from the floor.|Return toward a straight, comfortable body line without throwing into a large backbend.|Lower slowly and stop before momentum or discomfort determines the range.",
       "Exhale through the lifting phase and inhale through the controlled return.", "Pull")
recipe("leg_curl standing_leg_curl glute_ham dumbbell_leg_curl",
       "Choose the named seated, prone, standing or glute-ham support arrangement.|For machine curls, align the knees with the pivots and fit the lower-calf roller and thigh restraints.|For glute-ham work use appropriate foot restraints and assistance; do not improvise an unstable dumbbell hold between the feet.",
       "Bend the working knees to draw the heels toward the body through a controlled range.|Keep the hips and pelvis supported rather than lifting them to finish.|Pause before the movement pulls on an uncomfortable end position.|Straighten the knees slowly and repeat without letting the load drop.",
       "Exhale through the curl and inhale as the legs return.", "Legs")
recipe("leg_extension",
       "Fit the back pad so the thighs are supported and knees align with the machine pivots.|Set the lower-shin roller above the ankles and choose a comfortable starting bend.|Select a light load and hold the intended handles with the pelvis on the seat.",
       "Extend the knees smoothly to lift the roller.|Finish without kicking into a forced lockout.|Bend the knees slowly through a comfortable return range.|Keep the hips supported and repeat without momentum or stack impact.",
       "Exhale as the knees extend and inhale on the return.", "Legs")
recipe("abduction adduction cable_hip",
       "Choose the named seated machine or low-pulley ankle-cuff arrangement.|Fit pads or cuffs and stabilize the torso and pelvis.|For standing cable work, use a fixed hand support and keep the stance foot clear of the cable.",
       "Move the working thigh in the prescribed direction: outward for abduction, inward for adduction, backward for a kickback.|Use a comfortable arc without swinging or leaning the trunk.|Pause before the pelvis rotates or the stretch becomes uncomfortable.|Return slowly and complete the prescribed sides with the same alignment.",
       "Exhale as the thigh moves against resistance and inhale as it returns.", "Legs")
recipe("standing_calf supported_calf seated_calf",
       "Place the balls of the feet securely on the floor or intended calf platform.|Use stable support, fit any pads and rehearse the release or re-rack control before loading.|Keep knees bent for the seated version or comfortably near straight for a standing or leg-press version.",
       "Raise the heels through ankle extension without rolling onto the outer feet.|Pause at a controlled top position.|Lower the heels gently to a comfortable stretch without bouncing.|Maintain the chosen knee angle throughout and re-rack safely at the end.",
       "Exhale as the heels rise and inhale as they lower.", "Legs")
recipe("crunch situp reverse_crunch bicycle v_up leg_raise hanging_raise",
       "Choose the named floor, decline, hanging, cable or machine position.|Support the body or establish a secure hang, and set a comfortable starting range.|Keep the neck relaxed and choose a leg position or lever length that permits pelvic control.",
       "Perform the named trunk or hip action with a controlled pelvis.|For crunches and sit-ups, curl the ribs toward the pelvis; for reverse crunches, curl the pelvis toward the ribs.|For leg raises, raise the thighs without swinging; for bicycles or V-ups, coordinate the specified alternating or combined movement.|Lower slowly and stop the range before the back arches or momentum takes over.",
       "Exhale through the curl or lift and inhale during the controlled return.", "Core")
recipe("plank side_plank hollow rollout pallof",
       "Choose the named floor support, side support, hollow position or rollout apparatus.|Use a clear, nonslip surface and position the shoulders comfortably above or in relation to their support.|Set a body angle, lever length or reach you can hold while breathing.",
       "Brace the trunk to resist sagging, side-bending or rotation as required.|For a hold, maintain the prescribed position without letting the pelvis drift.|For a rollout, move forward only as far as you can reverse without the lower back sagging.|End the hold or return to the starting position as soon as trunk control deteriorates.",
       "Keep breathing steadily during holds; exhale through the return of a controlled rollout.", "Core")
recipe("jump_squat",
       "Use a clear, level, nonslip landing area and begin with bodyweight.|Set a comfortable squat stance and rehearse quiet landings before repeated jumps.|Choose a low jump height that allows stable knee and foot alignment.",
       "Lower into a controlled squat and drive upward through the feet.|Leave the floor without throwing the trunk backward.|Land softly by bending through the hips, knees and ankles.|Reset balance before the next repetition and stop before landings become uncontrolled.",
       "Breathe naturally between jumps and exhale through the upward effort.", "Legs", "Intermediate")
recipe("pass_through",
       "Use a sound resistance band and a wide grip that permits comfortable shoulder movement.|Stand tall with stable feet and soft elbows.|Keep the ribs controlled and choose a tension that does not force the shoulders.",
       "Raise the band overhead through a comfortable arc.|Move it farther only while the shoulders and trunk remain controlled; a complete pass behind the body is not required.|Reverse along the same gentle path.|Widen the grip or shorten the arc if you need to arch, bend the elbows or push through discomfort.",
       "Breathe slowly throughout; do not strain or hold the breath to gain range.", "Mobility")
recipe("child_cobra child_reach lunge_arch side_lunge_stretch hamstring_stretch world_stretch leg_swing",
       "Choose the named stretch or mobility position on a stable surface.|Use padding or a fixed hand support where needed for comfort and balance.|Begin with a small, comfortable range rather than forcing the endpoint.",
       "Move slowly into the position described by the exercise name.|Keep the intended joint motion controlled rather than gaining range through a large back arch.|For dynamic movements, travel smoothly in and out; for held positions, relax without bouncing.|Return carefully and repeat or change sides as the program prescribes.",
       "Keep slow, relaxed breathing throughout the movement or hold.", "Mobility")
recipe("foam_roll",
       "Choose a foam roller and a clear floor area where you can support your bodyweight.|Select the muscle region named in your program or by your trainer; avoid joints, bony prominences and direct neck pressure.|Use the hands or feet to limit how much pressure you apply.",
       "Place the intended muscle region gently against the roller.|Make short, slow passes while controlling pressure with the supporting limbs.|Reposition rather than grinding directly on a painful point.|Finish or change regions without rolling over an acute injury or an area with numbness or swelling.",
       "Breathe normally and reduce pressure if you find yourself tensing or holding the breath.", "Recovery")
recipe("clean",
       "Learn the clean and front-rack catch with qualified instruction before adding load.|Use a suitable bar, clear lifting space and a load you can handle through both clean and press.|Rehearse the pull, catch, overhead clearance and controlled return separately.",
       "From the coached clean start, drive through the feet and extend the hips to bring the bar upward close to the body.|Receive the bar in a controlled front-rack position rather than catching it on extended wrists.|Stabilize the feet and trunk, then press from the shoulders to a comfortable overhead finish.|Lower to the front rack and return the bar using the controlled sequence taught for the exercise.",
       "Reset breathing and trunk support between the clean and press phases; learn loaded bracing with a coach.", "Full body", "Advanced")

# These overrides remove ambiguity for named variants and multi-position warm-ups.
OVERRIDES = {
    "Arnold Press": dict(steps=["Start seated with light dumbbells near the front of the shoulders, palms facing inward.", "Press upward while rotating the palms toward a comfortable forward-facing position.", "Finish without forcing a large back arch or painful shoulder rotation.", "Lower slowly while reversing the hand rotation and maintaining support."]),
    "Barbell JM Press": dict(steps=["Use a very light bar, a stable flat bench, appropriate safeties and qualified instruction for this hybrid press.", "From a controlled pressing start, bend through the elbows and lower the bar toward the upper-chest/chin region on the path you were taught.", "Keep the movement controlled rather than dropping the bar toward the face.", "Extend through the elbows to return; stop before the wrists or elbows become uncomfortable."], difficulty="Advanced"),
    "Sissy Squat": dict(steps=["Hold a fixed support and begin with bodyweight.", "Bend the knees and allow the body to lean back only through a small range you can control.", "Keep the hips relatively extended as prescribed without dropping into the knee bend.", "Return smoothly using the support as needed; stop for knee pain."], difficulty="Intermediate"),
    "Child pose to cobra": dict(steps=["From kneeling, ease the hips toward the heels and reach the arms forward into a comfortable child's pose.", "Move forward and lower into a supported prone position without rushing the transition.", "Use the forearms or hands to lift into a gentle cobra-like extension, keeping the neck relaxed and hips supported as appropriate.", "Ease back and repeat only within a comfortable shoulder and back range."]),
    "Child pose with reach": dict(steps=["Start kneeling on a padded surface and ease the hips toward the heels.", "Reach both hands forward while keeping the shoulder and back position comfortable.", "If a side reach is prescribed, walk the hands slightly to that side without forcing the shoulder.", "Return to center gently and repeat on the other side if prescribed."]),
    "Low lunge arch": dict(steps=["Step into a short, supported low lunge and pad the rear knee if it rests on the floor.", "Keep the pelvis controlled and move gently forward to lengthen the front of the rear hip.", "Add only a small, comfortable upward reach or upper-trunk extension without forcing the lower back.", "Ease out slowly and change sides as prescribed."]),
    "Side lunge stretch": dict(steps=["Stand in a comfortable wide stance with feet on a nonslip surface.", "Bend one knee and shift the hips toward that side while the other leg stays comfortably lengthened.", "Stop at a mild inner-thigh stretch with the bent knee tracking with its foot.", "Return to center and repeat on the other side without bouncing."]),
    "Standing hamstring stretch": dict(steps=["Place one heel on a low, stable support or take a short staggered stance with the front heel grounded.", "Keep a slight bend in the working knee and hinge gently forward from the hips.", "Stop at a mild stretch behind the thigh without rounding the back to reach farther.", "Ease upright and repeat on the other side as prescribed."]),
    "Leg swing": dict(steps=["Hold a fixed support and stand tall on one leg.", "Swing the other leg gently forward and backward for the sagittal variation, or across and outward for the side-to-side variation.", "Keep the pelvis and trunk controlled rather than throwing the leg to maximum height.", "Reduce the range if balance changes, then switch sides as prescribed."]),
    "World’s greatest stretch": dict(steps=["Step into a comfortable long lunge and place the hands on a stable support or the floor as your mobility allows.", "Gently explore the front hip and inner-thigh stretch without forcing the position.", "Rotate the upper trunk and reach one arm upward only through a controlled range, then return the hand to support.", "Ease out of the lunge and repeat the sequence on the other side."]),
    "Bicycle Crunches": dict(steps=["Lie on a padded floor with knees bent and hands lightly supporting, not pulling, the head.", "Curl and rotate the trunk toward one bent knee while the opposite leg reaches only as far as pelvic control permits.", "Return smoothly and alternate sides without jerking the neck.", "Shorten the leg reach if the back arches or the movement becomes a rapid pedal."]),
    "Hollow Hold": dict(steps=["Lie on a padded surface, draw the ribs toward the pelvis and start with bent knees.", "Lift the head and shoulders gently and extend the legs or arms only as far as trunk control permits.", "Hold without letting the lower back arch away from your controlled position.", "Keep breathing and lower the limbs as soon as posture deteriorates."]),
    "Barbell rollout": dict(steps=["Kneel on a suitable pad behind an evenly loaded, smoothly rolling barbell on a clear nonslip floor.", "Hold the bar securely and brace the trunk before rolling it a short distance forward.", "Stop the reach before the lower back sags or the shoulders lose control.", "Return the bar slowly using trunk and shoulder control, without jerking the hips backward."]),
    "Seated behind-the-neck press": dict(setup=["This variation requires suitable shoulder mobility and individual instruction; choose a front-of-body press if the position is not comfortable.", "Use a stable seat and very light load with appropriate spotting and safeties.", "Do not force the neck forward or the arms behind a range you can control."], difficulty="Advanced"),
}
