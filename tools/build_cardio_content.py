"""Build the offline cardio catalog, diagrams and sourced HIIT sessions. No dependencies."""
import json
from pathlib import Path
from build_muscle_maps import key, MUSCLES, vector as muscle_vector
from equipment_setups import vector as setup_vector

MAIN = Path(__file__).resolve().parents[1] / "app/src/main"
TABATA = "https://www.ritsumei.ac.jp/research/radiant/eng/2019-2021/story5.html/"
NORWEGIAN = "https://www.ntnu.edu/cerg/advice"
MCMASTER = "https://news.mcmaster.ca/no-time-to-get-fit-think-again/"
ROWING = "https://www.concept2.com/blog/10-ways-to-row-like-a-pro"
STOP = "Stop for chest pain, faintness, unusual breathlessness or sharp pain. Get appropriate medical advice before vigorous intervals if you have symptoms or a relevant health condition."

# Each numbered callout matches the native setup diagram and its four steps.
EQUIPMENT = {
    "treadmill": ("Treadmill", ["Running machine"], [("Safety stop", "Locate Stop and attach the safety clip to clothing if provided; test it as instructed by the manufacturer."), ("Starting position", "Mount using the fixed platforms and handrails according to the machine's placard; start the belt at its lowest walking speed."), ("Speed and incline", "Increase one setting gradually while keeping a natural gait. Slow down instead of hanging from the rails."), ("Dismount", "Reduce speed, stop the belt fully, and use the rails to step off. Never jump onto or off a moving belt.")], [(583, 166), (58, 225), (566, 55), (584, 339)]),
    "stairs": ("Stair machine", ["Stair climber", "Stairmill", "Stepmill"], [("Mount safely", "Find the stop control and use fixed steps and handrails to mount with the machine stopped, following its placard."), ("Foot placement", "Face forward and place enough of each foot securely on the tread; keep shoes clear of moving edges."), ("Step speed", "Begin at the lowest pace and increase gradually. Stay upright with light rail contact rather than supporting bodyweight on the arms."), ("Stop and descend", "Slow and stop the steps fully before dismounting with the handrails. Record steps/min and floors from the console when available.")], [(154, 301), (180, 236), (538, 45), (526, 163)]),
    "bike": ("Exercise bike", ["Stationary bike", "Upright bike", "Indoor cycle"], [("Seat height", "With the pedal farthest away, keep a small knee bend without rocking the hips. Adjust and lock the seat while stopped."), ("Reach and feet", "Set the seat and handlebar reach for relaxed shoulders. Secure shoes using the intended pedal straps; keep laces clear."), ("Resistance", "Start at low resistance and learn the model's braking controls. Fixed-gear pedals keep moving with the flywheel; use its brake before dismounting."), ("Console", "Select time and distance display units. Record resistance as the machine's setting; levels are not comparable across models.")], [(190, 115), (540, 133), (513, 271), (533, 49)]),
    "rower": ("Rowing machine", ["Indoor rower", "Row ergometer", "RowErg"], [("Foot supports", "Adjust the heel cups so the straps cross the widest part of the feet. Snug the straps without cutting circulation."), ("Seat and travel", "Check the seat slides smoothly and the rail is clear. Sit balanced with relaxed shoulders and a secure, light handle grip."), ("Resistance or damper", "Start with an easy setting and learn the stroke. An air damper changes airflow and stroke feel; it is not a universal resistance or intensity scale."), ("Monitor", "Choose time and distance, normally meters, and begin with easy strokes. Park the handle safely and release the straps before standing.")], [(331, 186), (477, 215), (47, 185), (327, 57)]),
}

COACHING = {
    "locomotion": dict(primary="quads glutes calves", assisting="hamstrings hip_flexors soleus", stabilizers="abs obliques glute_med erectors",
        groups=["Quads", "Glutes", "Calves"],
        setup=["Choose a clear route with secure footing and suitable shoes.", "For a treadmill, follow its setup guide, safety-stop instructions and gradual start procedure.", "Begin easily and set a pace that lets you maintain a relaxed, controlled gait."],
        steps=["Move forward with a tall, relaxed trunk and natural arm swing.", "Use comfortable strides rather than reaching far ahead of the body.", "Build pace or incline gradually; on a treadmill, keep position on the belt without holding your bodyweight on the rails.", "Slow gradually at the end; stop a treadmill fully before stepping off."],
        mistakes=["Overstriding: shorten the stride and keep foot contacts controlled.", "Increasing pace and incline abruptly: change one setting gradually.", "Leaning on treadmill rails: reduce speed or incline until you can move with light balance support only."],
        prevention=["Build total duration gradually before adding harder intervals.", "Use an easy warm-up and cool-down, and reduce impact if joints become painful."], source=NORWEGIAN),
    "stairs": dict(primary="quads glutes", assisting="calves hamstrings soleus", stabilizers="abs obliques glute_med erectors",
        groups=["Quads", "Glutes"],
        setup=["Choose dry, clear stairs with a usable handrail, or follow the stair machine's setup guide.", "Wear secure footwear and locate the machine stop control before starting.", "Start at a slow pace that allows secure foot placement."],
        steps=["Face forward and step onto each tread with controlled foot placement.", "Push through the supporting leg while keeping the trunk upright.", "Use the handrail for balance; reduce pace if you need to hang from it.", "Slow before ending. Stop a machine fully before dismounting; descend real stairs carefully."],
        mistakes=["Skipping treads at an uncontrolled pace: return to single, secure steps.", "Supporting bodyweight on the rails: lower speed and restore upright posture.", "Turning around on moving steps: remain forward-facing and stop before dismounting."],
        prevention=["Start with short bouts and increase time as balance and conditioning allow.", "Use a secure handrail and keep clear of moving step edges."], source=NORWEGIAN),
    "bike": dict(primary="quads glutes", assisting="hamstrings calves soleus", stabilizers="abs obliques erectors",
        groups=["Quads", "Glutes"],
        setup=["On a stationary bike, adjust and lock the seat so the knee remains slightly bent at the farthest pedal position.", "Set a comfortable handlebar reach and secure feet using the intended pedals or straps.", "Begin at easy resistance. Outdoors, use a suitable helmet, inspect the bicycle and follow local road rules."],
        steps=["Pedal smoothly with knees tracking comfortably over the feet.", "Keep the hips steady and the shoulders relaxed rather than gripping the bars tightly.", "Adjust resistance gradually while maintaining controlled pedaling.", "Ease the pace to finish; on a fixed-gear bike, use the brake and wait for the flywheel and pedals to stop before dismounting."],
        mistakes=["Rocking the hips: check seat height and reduce resistance.", "Grinding against excessive resistance: lower the setting and restore smooth pedal motion.", "Dismounting while fixed-gear pedals turn: brake and wait for a complete stop."],
        prevention=["Recheck seat adjustment if knee or hip discomfort appears.", "Learn the machine's braking behavior before attempting hard intervals."], source=MCMASTER),
    "rower": dict(primary="quads glutes lats", assisting="hamstrings biceps traps_mid rear_delts calves", stabilizers="abs obliques erectors rotator_cuff",
        groups=["Quads", "Glutes", "Back"],
        setup=["Adjust the footrests so the straps cross the widest part of each foot and secure them comfortably.", "Check the seat and rail, take a light handle grip and choose an easy resistance or damper setting.", "At the catch, reach with relaxed shoulders, a controlled hip hinge and shins close to vertical without forcing compression."],
        steps=["Push with the legs first while keeping the arms long and the trunk supported.", "As the legs extend, swing the torso slightly back from the hips, then draw the handle toward the lower ribs.", "Recover in reverse order: arms away, hinge the torso forward, then bend the knees to slide forward.", "Repeat smoothly without slamming the seat or yanking the chain; ease off before parking the handle."],
        mistakes=["Pulling with the arms before driving the legs: restore the legs-body-arms sequence.", "Rounding or throwing the lower back: shorten the reach and control the hip hinge.", "Treating maximum damper as the best workout: learn technique at an easy setting; record the machine setting separately from effort."],
        prevention=["Use slower practice strokes before increasing power or stroke rate.", "Keep the grip light and stop if the back or wrists become painful."], source=ROWING),
    "reps": dict(primary="quads calves glute_med", assisting="glutes front_delts side_delts soleus", stabilizers="abs obliques erectors",
        groups=["Quads", "Calves", "Glutes"],
        setup=["Use a clear, nonslip surface with overhead and side clearance.", "Wear secure shoes and start with feet together, arms relaxed by the sides.", "Choose small jumps or use alternating step-outs if a lower-impact option is needed."],
        steps=["Make a small jump to move the feet apart while raising the arms comfortably out and up.", "Land softly with knees tracking with the feet rather than collapsing inward.", "Jump back to feet together as the arms return to the sides; this out-and-back cycle is one rep.", "Repeat at a controllable rhythm, stopping the set if landing quality or balance deteriorates."],
        mistakes=["Landing with stiff knees: soften the landing and reduce jump height.", "Knees collapsing inward: slow down and keep knees aligned with the feet.", "Forcing the arms overhead through pain: use a comfortable reach or a step-jack variation."],
        prevention=["Build repetitions gradually and allow enough recovery between sets.", "Use step jacks for less impact if jumping causes discomfort."], source="https://www.acefitness.org/resources/everyone/exercise-library/"),
}

def write(name, data):
    (MAIN / f"assets/{name}.json").write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")

def main():
    equipment, setup_guides, exercises, safety, assignments, maps, configs = [], {}, [], {}, {}, {}, {}
    for id, (name, aliases, steps, markers) in EQUIPMENT.items():
        eid = "eq_cardio_" + id
        image = "setup_cardio_" + id
        equipment.append(dict(id=eid, name=name, alternateNames=aliases, category="Cardio", howToUse=[s[1] for s in steps], notes="Controls vary by model; follow its placard and staff orientation."))
        g = dict(scene=id, steps=[dict(title=t, text=v) for t, v in steps], markers=markers, caution=STOP)
        (MAIN / f"res/drawable/{image}.xml").write_text(setup_vector(g), encoding="utf-8")
        setup_guides[eid] = dict(name=name, image=image, steps=g["steps"], caution=STOP)
    rows = [("Walking", "locomotion", None), ("Jogging", "locomotion", None), ("Running", "locomotion", None),
            ("Bicycling", "bike", None), ("Rowing", "rower", "rower"), ("Jumping jacks", "reps", None),
            ("Climbing stairs", "stairs", None), ("Treadmill walking", "locomotion", "treadmill"),
            ("Treadmill jogging", "locomotion", "treadmill"), ("Treadmill running", "locomotion", "treadmill"),
            ("Stationary bicycling", "bike", "bike"), ("Stair machine climbing", "stairs", "stairs")]
    for name, tracking, machine in rows:
        c = COACHING[tracking]
        pid = "cardio_" + tracking
        eid = "ex_cardio_" + key(name)
        ids = ["eq_cardio_" + machine] if machine else []
        profile = dict(safetyTips=["Warm up at an easy pace and build intensity gradually.", STOP], injuryPrevention=c["prevention"], mistakes=c["mistakes"])
        safety[pid] = profile
        assignments[key(name)] = [dict(label=name, profile=pid, equipmentIds=ids)]
        setup = [dict(equipmentId=q, position=setup_guides[q]["steps"][0]["text"], notes=[s["text"] for s in setup_guides[q]["steps"][1:]]) for q in ids]
        configs[eid] = setup
        exercises.append(dict(id=eid, name=name, equipmentIds=ids, primaryMuscles=c["groups"], movement="Cardio", tracking=tracking,
            setup=c["setup"], steps=c["steps"], breathing="Breathe rhythmically; reduce intensity if you cannot maintain control. Do not hold your breath through intervals.",
            equipmentSetup=setup, equipmentNotes="Use the linked machine setup guide." if machine else "Outdoor or bodyweight option; use the separate machine variation when training indoors." if tracking != "reps" else "No equipment needed; use a clear, nonslip surface.",
            target="; ".join(MUSCLES[m] for m in c["primary"].split()), source=c["source"], guideKind="cardio", **profile))
        maps[pid] = dict(primary=c["primary"].split(), assisting=c["assisting"].split(), stabilizers=c["stabilizers"].split(), kind="cardio", source=c["source"],
            note="Typical muscle roles during controlled cardio technique. These colors describe movement roles, not measured activation or individual training intensity.")
    for pid, p in maps.items():
        assert not (set(p["primary"]) & set(p["stabilizers"]))
        (MAIN / f"res/drawable/muscle_{pid}.xml").write_text(muscle_vector(p), encoding="utf-8")
    write("cardio", dict(equipment=equipment, exercises=exercises))
    write("cardio_guides", dict(equipment=setup_guides, profiles=safety, exercises=assignments, exerciseSetups=configs,
        sources=[dict(title="Indoor rowing technique and setup", url=ROWING), dict(title="HIIT exercise guidance", url=NORWEGIAN)], equipmentCheck="Read the model's controls before starting.", stopRule=STOP))
    write("cardio_muscle_maps", dict(muscles=MUSCLES, profiles=maps, exercises={key(n): [dict(label=n, profile="cardio_"+t, image="muscle_cardio_"+t)] for n, t, _ in rows}))

    def block(name, seconds, note, phase="Working"):
        return dict(phase=phase, exercise=name, sets="1", reps="Timed", repsNote="Record actual duration; use a clock or the machine's interval timer.", rest="Included in the recovery blocks", restSec=0, setCount=1, notes=note, substitution="", durationSec=seconds)

    tabata = [block("Stationary bicycling", 300, "Easy pedaling; adjust seat, pedals and brake before the intervals.", "Warm-up")]
    for i in range(8):
        tabata += [block("Stationary bicycling", 20, f"Hard effort {i+1}/8. Maintain controlled pedaling; this demanding format is for experienced exercisers."),
                   block("Stationary bicycling", 10, f"Recovery {i+1}/8. Reduce effort and follow the bike's braking instructions; never step off moving pedals.")]
    tabata += [block("Stationary bicycling", 300, "Easy pedaling and gradual slowing.", "Cool-down")]
    norwegian = [block("Treadmill walking", 600, "Progress from easy to brisk; attach the safety clip if provided.", "Warm-up")]
    for i in range(4):
        norwegian.append(block("Treadmill running", 240, f"Hard interval {i+1}/4. CERG recommends about 85–95% of maximum heart rate toward the end, not an all-out sprint. A brisk uphill walk can be used instead; choose a controllable pace."))
        if i < 3: norwegian.append(block("Treadmill walking", 180, "Active recovery at an easy pace, approximately 70% of maximum heart rate. Reduce belt speed gradually."))
    norwegian += [block("Treadmill walking", 300, "Gradually return to an easy walking pace before stopping.", "Cool-down")]
    minute = [block("Stationary bicycling", 120, "Easy pedaling.", "Warm-up")]
    for i in range(3):
        minute.append(block("Stationary bicycling", 20, f"Sprint {i+1}/3. The study used all-out cycling; learn the technique and obtain individual guidance before attempting maximal efforts."))
        if i < 2: minute.append(block("Stationary bicycling", 120, "Easy pedaling recovery."))
    minute += [block("Stationary bicycling", 180, "Easy pedaling; stop the flywheel safely before dismounting.", "Cool-down")]
    programs = []
    for id, name, author, url, blocks, duration, description in [
        ("HIIT_TABATA", "Tabata 20/10 Cycling", "Izumi Tabata / Ritsumeikan University", TABATA, tabata, "14 minutes", "Eight 20-second efforts with 10-second recovery periods form the well-known four-minute Tabata block. GymClues adds a five-minute warm-up and five-minute cool-down. The original laboratory intensity was demanding; this app does not prescribe a universal resistance or promise the original study's results."),
        ("HIIT_4X4", "Norwegian 4×4 Intervals", "Cardiac Exercise Research Group / NTNU", NORWEGIAN, norwegian, "40 minutes", "Four four-minute hard intervals separated by three minutes of active recovery, preceded by a ten-minute warm-up and followed by five minutes easy. This treadmill implementation uses running for hard efforts; brisk uphill walking is an option when it produces the intended effort with good control. Speeds and inclines must be individualized."),
        ("HIIT_ONE_MINUTE", "McMaster One-Minute Workout", "Martin Gibala and colleagues / McMaster University", MCMASTER, minute, "10 minutes", "Three 20-second cycling sprints provide one minute of hard effort within a ten-minute session: two minutes easy to start, two minutes easy between sprints and three minutes easy to finish. This is sprint interval training, included here under HIIT. The study used supervised all-out cycling; a lower-intensity adaptation is not the identical research protocol."),
    ]:
        days = [dict(day=d, session=name if d in (1, 3, 5) else "Recovery day", training=d in (1, 3, 5), items=blocks if d in (1, 3, 5) else []) for d in range(1, 8)]
        programs.append(dict(id=id, name=name, split="HIIT", author=author, level="Advanced" if id != "HIIT_4X4" else "Intermediate", duration=duration+" per session; 4-week app schedule", description=description,
            schedule="Three sessions per seven-day cycle, with recovery days between. This four-week schedule is a GymClues scheduling choice, not a reproduction of a complete research study.",
            progression="Keep the work/recovery timings. Begin only when ready for vigorous exercise; individualize effort and reduce intensity if control deteriorates. Do not increase speed, resistance and duration together.",
            rest="Easy recovery between efforts; avoid stacking hard interval sessions on consecutive days.", sourceUrl=url, cycleLength=7, totalWeeks=4, days=days,
            notes=[dict(topic="Safety", text=STOP), dict(topic="Timing and logging", text="Use a clock or the machine's interval mode. Record actual time and machine settings for each work and recovery block; the app does not control the machine."), dict(topic="Source and adaptations", text="The source supplies the interval format. Warm-up/cool-down additions and the four-week calendar are stated in the description and schedule.")]))
    assert [sum(b["durationSec"] for b in p["days"][0]["items"]) for p in programs] == [840, 2400, 600]
    names = {e["name"] for e in exercises}
    assert all(b["exercise"] in names for p in programs for d in p["days"] for b in d["items"])
    assert len(exercises) == 12 and len(equipment) == 4
    write("hiit_programs", dict(programs=programs))
    print("Built 12 cardio exercise guides, 4 equipment guides, 5 muscle maps and 3 HIIT programs.")

if __name__ == "__main__":
    main()
