"""Add complete catalog pages for program movements, keeping original IDs and prescriptions.

Run after build_training_guides.py. Uses authored recipes, explicit equivalence
aliases and the existing primary-mover maps; no remote data or runtime guessing.
"""
import copy
import html
import json
from collections import Counter
from pathlib import Path
from build_muscle_maps import ASSIGNMENTS, PROFILES, MUSCLES, key
from program_exercise_aliases import ALIASES, CHOICES, norm
from program_exercise_content import RECIPES, OVERRIDES

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main"
GROUP = {
    "pecs": "Chest", "upper_pecs": "Chest", "front_delts": "Shoulders", "side_delts": "Shoulders",
    "rear_delts": "Shoulders", "rotator_cuff": "Shoulders", "biceps": "Biceps", "brachialis": "Biceps",
    "triceps": "Triceps", "forearm_flexors": "Forearms", "forearm_extensors": "Forearms", "brachioradialis": "Forearms",
    "lats": "Back", "traps_upper": "Back", "traps_mid": "Back", "traps_lower": "Back", "rhomboids": "Back",
    "erectors": "Back", "serratus": "Chest", "abs": "Core", "deep_core": "Core", "obliques": "Core",
    "glutes": "Glutes", "glute_med": "Glutes", "hip_flexors": "Hip flexors", "adductors": "Adductors",
    "quads": "Quads", "hamstrings": "Hamstrings", "calves": "Calves", "soleus": "Calves", "tibialis": "Shins",
}
EXTRA = {"One-arm barbell row": "one_arm_row", "Trap-bar shrug": "shrug",
         "Seated rope overhead triceps extension": "overhead_extension", "Seated EZ-bar triceps extension": "overhead_extension"}
DEFAULT_EQUIPMENT = {
    "bench": ["eq_x_barbell", "eq_adjustable_bench"], "incline": ["eq_adjustable_bench"],
    "decline": ["eq_x_barbell", "eq_adjustable_bench"], "close_press": ["eq_x_barbell", "eq_adjustable_bench"],
    "machine_press": ["eq_chest_press"], "machine_incline": ["eq_x_incline_chest_press_machine"],
    "fly": ["eq_dumbbells", "eq_adjustable_bench"], "incline_fly": ["eq_x_cable", "eq_adjustable_bench"],
    "cable_fly": ["eq_x_cable"], "pec_deck": ["eq_x_pec_deck_machine"], "pullover": ["eq_dumbbells", "eq_adjustable_bench"],
    "row": ["eq_x_barbell"], "one_arm_row": ["eq_dumbbells", "eq_adjustable_bench"],
    "supported_row": ["eq_x_seated_row_machine"], "seated_row": ["eq_x_cable"], "one_arm_cable_row": ["eq_x_cable"],
    "pulldown": ["eq_lat_pulldown"], "one_arm_pulldown": ["eq_x_cable"], "straight_arm_pull": ["eq_x_cable"],
    "overhead_press": ["eq_x_barbell"], "seated_press": ["eq_dumbbells", "eq_adjustable_bench"],
    "machine_shoulder_press": ["eq_x_shoulder_press_machine"], "lateral_raise": ["eq_dumbbells"],
    "seated_raise": ["eq_dumbbells", "eq_adjustable_bench"], "fdl_raise": ["eq_dumbbells", "eq_adjustable_bench"],
    "rear_fly": ["eq_dumbbells"], "reverse_deck": ["eq_x_pec_deck_machine"], "face_pull": ["eq_x_cable"],
    "supported_curl": ["eq_dumbbells", "eq_adjustable_bench"], "hammer_curl": ["eq_dumbbells"], "zottman": ["eq_dumbbells"],
    "overhead_extension": ["eq_dumbbells"], "skullcrusher": ["eq_x_ez_bar", "eq_adjustable_bench"],
    "machine_triceps": ["eq_x_triceps_extension_machine"], "pushdown": ["eq_x_cable"], "kickback": ["eq_dumbbells"],
    "squat": ["eq_x_barbell"], "front_squat": ["eq_x_barbell"], "machine_squat": ["eq_x_hack_squat_machine"],
    "leg_press": ["eq_leg_press"], "smith_lunge": ["eq_smith"], "deadlift": ["eq_x_barbell"],
    "rack_pull": ["eq_x_barbell"], "rdl": ["eq_x_barbell"], "good_morning": ["eq_x_barbell"],
    "hip_thrust": ["eq_x_barbell", "eq_adjustable_bench"], "pullthrough": ["eq_x_cable"],
    "back_extension": ["eq_x_45_degree_extension_bench"], "leg_curl": ["eq_x_seated_leg_curl_machine"],
    "leg_extension": ["eq_x_leg_extension_machine"], "abduction": ["eq_x_hip_abduction_machine"],
    "adduction": ["eq_x_hip_adduction_machine"], "seated_calf": ["eq_adjustable_bench"],
    "rollout": ["eq_x_barbell"], "clean": ["eq_x_barbell"],
}
EQUIPMENT_NOTES = {
    "pullup": "Use a fixed, rated pull-up bar with safe steps and landing clearance; add assistance or external weight only as prescribed.",
    "chinup": "Use a fixed, rated pull-up bar and an underhand grip; additional weight or assistance is optional only when prescribed.",
    "dip": "Use stable dip bars or the prescribed assisted dip station. Free dip bars and a seated dip machine are different setups.",
    "machine_dip": "Requires a seated triceps-dip machine; do not substitute the moving platform of an assisted bodyweight dip without changing the exercise.",
    "inverted_row": "Requires a securely fixed bar or suspension anchor explicitly rated for bodyweight rows.",
    "pull_apart": "Requires an intact resistance band and enough clear space for the full reach.",
    "pass_through": "Requires an intact resistance band with a wide, comfortable grip; never force a pass behind the body.",
    "external_rotation": "Use a very light resistance band or cable attachment with the intended elbow support.",
    "internal_rotation": "Use a very light resistance band or cable attachment with the intended elbow support.",
    "glute_ham": "Requires a glute-ham developer with correctly fitted foot restraints; a 45-degree extension bench is not the same apparatus.",
    "reverse_hyper": "Requires a suitable reverse-hyperextension machine or supported setup approved for this movement; do not improvise an unstable bench arrangement.",
    "foam_roll": "Requires a suitable foam roller and space on a firm floor; roll the region prescribed rather than the neck or bony joints.",
    "stepup": "Requires a stable platform explicitly rated for stepping. An adjustable exercise bench is not automatically suitable.",
    "sissy_squat": "Use a fixed hand support or purpose-built sissy-squat station; begin with bodyweight and a short controlled range.",
    "standing_leg_curl": "Requires a suitable standing leg-curl machine or the exact prescribed cuff setup; fit its knee axis and roller before loading.",
    "high_row": "Requires the supported high-row machine specified by the workout. Adjust its pads and handles before loading.",
}
PROFILE_EXTRA_GROUPS = {"carry": ["Forearms"], "wrist_rotation": ["Forearms"], "reverse_curl": ["Biceps", "Forearms"],
                        "front_raise": ["Shoulders"], "y_raise": ["Back", "Shoulders"], "pallof": ["Core"],
                        "chop": ["Core"], "rotation": ["Core"], "cable_hip": ["Glutes"], "dumbbell_leg_curl": ["Hamstrings"]}


def unique(values):
    return list(dict.fromkeys(values))


def equipment_for(name, profile):
    n = name.lower()
    ids = list(DEFAULT_EQUIPMENT.get(profile, []))
    if "dumbbell" in n:
        ids = ["eq_dumbbells"] + [i for i in ids if i == "eq_adjustable_bench"]
    elif "ez-bar" in n or "ez bar" in n:
        ids = ["eq_x_ez_bar"] + [i for i in ids if i == "eq_adjustable_bench"]
    elif "barbell" in n:
        ids = ["eq_x_barbell"] + [i for i in ids if i == "eq_adjustable_bench"]
    elif "trap-bar" in n or "trap bar" in n:
        ids = ["eq_x_trap_bar"]
    if "cable" in n or "rope" in n:
        ids = ["eq_x_cable"] + [i for i in ids if i == "eq_adjustable_bench"]
    if "smith" in n:
        ids = ["eq_smith"] + (["eq_adjustable_bench"] if "bench" in n or "overhead press" in n or "shoulder press" in n else [])
    if "floor" in n:
        ids = [i for i in ids if i != "eq_adjustable_bench"]
    if "standing" in n and profile in {"overhead_extension", "overhead_press", "shrug"}:
        ids = [i for i in ids if i != "eq_adjustable_bench"]
    if "seated" in n and any(i in ids for i in ("eq_dumbbells", "eq_x_ez_bar", "eq_x_barbell", "eq_x_cable")) and profile in {"overhead_extension", "seated_press", "supported_curl", "seated_raise", "seated_shrug", "seated_calf"}:
        ids.append("eq_adjustable_bench")
    if "lying leg curl" in n or "prone" in n and profile == "leg_curl":
        ids = ["eq_x_lying_leg_curl_machine"]
    if "machine" in n and profile == "supported_curl":
        ids = []  # A generic biceps-curl machine is not necessarily a preacher machine.
    if "machine" in n and profile == "supported_calf":
        ids = ["eq_x_standing_calf_raise_machine"]
    if "leg press" in n and profile == "supported_calf":
        ids = ["eq_leg_press"]
    if "assisted" in n and profile == "dip":
        ids = ["eq_x_assisted_dip_machine"]
    if "pendulum" in n:
        ids = []  # No fabricated catalog match to the mechanically different hack squat.
    if "goblet" in n or "plie" in n:
        ids = ["eq_dumbbells"]
    if "bodyweight" in n and profile in {"deep_squat", "jump_squat", "squat"}:
        ids = []
    if "behind-the-neck" in n: ids = ["eq_x_barbell", "eq_adjustable_bench"]
    if "one-arm barbell" in n: ids = ["eq_x_barbell"]
    if "t bar prone" in n: ids = []
    return unique(ids)


def configuration(equipment_id, name, profile, guides):
    q = guides["equipment"][equipment_id]
    config = dict(equipmentId=equipment_id, position=q["steps"][0]["text"], angle="", attachment="",
                  notes=["Lock adjustments and rehearse the intended range with light resistance before working sets."])
    if equipment_id == "eq_adjustable_bench":
        angle = "Low incline" if profile in {"incline", "incline_fly"} or "incline" in name.lower() else "Flat"
        if profile in {"seated_press", "overhead_extension"}: angle = "Upright or slightly reclined as prescribed"
        if "decline" in name.lower(): angle = "Decline only on a bench rated for it, with the required leg restraint"
        config.update(position="Set a stable, locked support position for this exercise; keep the head and torso supported as prescribed.", angle=angle)
    elif equipment_id == "eq_x_cable":
        high = profile in {"pushdown", "straight_arm_pull", "pulldown", "crunch"}
        height = "High" if high else "Low"
        if profile in {"face_pull", "rear_fly", "cable_fly"}: height = "Around shoulder or face height for the prescribed path"
        if profile == "overhead_extension": height = "High, or the prescribed overhead-extension setting"
        config.update(position=f"{height} pulley; lock the carriage and establish a stable stance or prescribed seat.",
                      attachment="Rope" if "rope" in name.lower() or profile == "face_pull" else "Single handle" if "one-arm" in name.lower() else "The bar or handle specified for this movement")
    return config


def main():
    catalog = [e for name in ("content", "library") for e in json.loads((MAIN / f"assets/{name}.json").read_text(encoding="utf-8"))["exercises"]]
    base_by_name = {}
    for e in catalog: base_by_name.setdefault(norm(e["name"]), e)
    guides = json.loads((MAIN / "assets/training_guides.json").read_text(encoding="utf-8"))
    programs = json.loads((MAIN / "assets/programs.json").read_text(encoding="utf-8"))["programs"]
    raw_names = sorted({i["exercise"] for p in programs for d in p["days"] for i in d["items"]})
    choices = {norm(n): v for n, v in CHOICES.items()}
    overrides = {norm(n): v for n, v in OVERRIDES.items()}
    source_names = {}
    for name in raw_names:
        source_names.setdefault(norm(ALIASES.get(norm(name), name)), name)
    for n in EXTRA: source_names.setdefault(norm(n), n)
    for names in CHOICES.values():
        for n in names: source_names.setdefault(norm(ALIASES.get(norm(n), n)), n)
    generated, name_ids, group_updates = {}, {}, {}

    # Canonicalization is explicit; stable base IDs and program legacy IDs remain usable.
    def canonical(name): return ALIASES.get(norm(name), name)
    for name in raw_names + list(EXTRA) + [n for values in CHOICES.values() for n in values]:
        canonical_name = canonical(name)
        existing = base_by_name.get(norm(name)) or base_by_name.get(norm(canonical_name))
        name_ids[norm(name)] = existing["id"] if existing else "ex_program_" + key(canonical_name)
        name_ids[norm(canonical_name)] = name_ids[norm(name)]

    def profile_ids(source):
        if source in EXTRA: return [EXTRA[source]]
        rows = guides["exercises"].get(key(source))
        assert rows, f"No authored coaching for {source}"
        return [v["profile"] for v in rows]

    # Correct old primary-group labels using the established mover taxonomy; never
    # promote stabilizers to primary groups just because a program's day trains back.
    for e in catalog:
        groups = []
        for p in profile_ids(e["name"]):
            groups += [GROUP[m] for m in PROFILES[p]["primary"]] if p in PROFILES else PROFILE_EXTRA_GROUPS[p]
        group_updates[e["id"]] = unique(groups)

    for canonical_key, source in source_names.items():
        name = canonical(source)
        id = name_ids.get(norm(name))
        if not id or id in {e["id"] for e in catalog}: continue
        profiles = profile_ids(source)
        is_choice = norm(source) in choices
        if norm(source) == norm("Barbell Clean and Press"):
            profiles = ["clean", "overhead_press"]
        primary = unique(m for p in profiles if p in PROFILES for m in PROFILES[p]["primary"])
        groups = unique([GROUP[m] for m in primary] + [g for p in profiles if p not in PROFILES for g in PROFILE_EXTRA_GROUPS[p]])
        assisting = unique(GROUP[m] for p in profiles if p in PROFILES for m in PROFILES[p]["assisting"] if GROUP[m] not in groups)
        main_profile = profiles[0]
        recipe = copy.deepcopy(RECIPES[main_profile])
        recipe.update(overrides.get(norm(source), {}))
        # A floor press, snatch-grip deadlift and split-stance movement retain their
        # own names and equipment; this is not a merge based only on target muscle.
        ids = equipment_for(name, main_profile)
        note = EQUIPMENT_NOTES.get(main_profile, "")
        if "pendulum" in name.lower(): note = "Requires the specified pendulum squat machine. Its moving arm and safety catches differ from a hack squat; get a model-specific orientation."
        if "machine" in name.lower() and main_profile == "supported_curl": note = "Requires the named biceps-curl machine. Align its elbow pivot and pad according to the machine's placard; a preacher machine may use a different setup."
        if "one-arm barbell" in name.lower(): note = "Requires a barbell held in an approved landmine/one-arm-row setup. Secure the intended anchor; never wedge a bar into an improvised corner."
        if "t bar prone" in name.lower(): note = "Requires a chest-supported T-bar row station with its own foot platform and chest pad; a seated cable row is not the same setup."
        if "bulgarian" in name.lower(): note += " Use a stable, rated rear-foot support and establish balance before adding load."
        if "box squat" in name.lower(): note += " Use a stable box rated for the load at the prescribed squat depth; do not use a movable seat."
        if "decline sit" in name.lower(): note += " Requires a decline abdominal bench with secure leg restraints; do not improvise with an unsupported adjustable bench."
        if "preacher" in name.lower(): note += " Requires a proper preacher pad supporting the upper arms; a flat bench alone is insufficient."
        if not ids and not note: note = "Use the prescribed bodyweight support or equipment variation. Check the original program when its exercise label does not specify the apparatus."
        linked_choices = [dict(name=canonical(n), exerciseId=name_ids[norm(n)]) for n in choices.get(norm(source), [])]
        if is_choice:
            ids = []
            recipe["setup"] = ["Choose one of the prescribed movement options below.", "Open that option's full guide and check its equipment and start position.", "Keep the program's set, repetition and loading prescription unless it explicitly says otherwise."]
            recipe["steps"] = ["Select the option you are actually performing; these are alternatives, not a sequence.", "Follow its individual movement, breathing and safety instructions.", "Return to the workout and record only the chosen movement's prescribed sets."]
            recipe["breathing"] = "Follow the breathing guidance on the selected movement's page."
            note = "The program offers alternative movements; their equipment is optional by choice, not all required together."
        safety = guides["profiles"][main_profile]
        muscle_source = source if key(source) in ASSIGNMENTS else next((n for n in raw_names if norm(canonical(n)) == norm(name) and key(n) in ASSIGNMENTS), "")
        generated[id] = dict(id=id, name=name, equipmentIds=ids, primaryMuscles=groups, secondaryMuscles=assisting,
            **recipe, target="; ".join(MUSCLES[m] for m in primary) or ", ".join(groups),
            why=PROFILES[main_profile].get("note", "") if main_profile in PROFILES else "",
            source=PROFILES[main_profile]["source"] if main_profile in PROFILES else "https://www.acefitness.org/resources/everyone/exercise-library/",
            safetyTips=safety["safetyTips"], injuryPrevention=safety["injuryPrevention"], mistakes=safety["mistakes"],
            equipmentSetup=[configuration(q, name, main_profile, guides) for q in ids],
            equipmentNotes=note, muscleMapName=muscle_source,
            guideKind=PROFILES[main_profile]["kind"] if main_profile in PROFILES else "strength", choices=linked_choices)
    all_ids = {e["id"] for e in catalog} | generated.keys()
    for e in generated.values():
        assert e["primaryMuscles"] and len(e["setup"]) >= 3 and len(e["steps"]) >= 3 and e["breathing"], e["name"]
        assert all(c["exerciseId"] in all_ids and c["exerciseId"] != e["id"] for c in e["choices"]), e["name"]
        assert all(q in guides["equipment"] for q in e["equipmentIds"]), e["name"]
    legacy = {"ex_program_" + key(n): name_ids[norm(n)] for n in raw_names}
    program_ids = {key(n): base_by_name[norm(n)]["id"] if norm(n) in base_by_name else "ex_program_" + key(n) for n in raw_names}
    assert all(name_ids[norm(n)] in all_ids for n in raw_names)
    output = dict(exercises=sorted(generated.values(), key=lambda e: e["name"].lower()),
                  nameAliases=name_ids, legacyIds=legacy, programIds=program_ids, primaryGroups=group_updates)
    (MAIN / "assets/program_exercises.json").write_text(json.dumps(output, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    preview = ROOT / "docs/program-exercises"
    preview.mkdir(parents=True, exist_ok=True)
    all_exercises = [dict(e, primaryMuscles=group_updates[e["id"]]) for e in catalog] + output["exercises"]
    grouped = {}
    for e in all_exercises:
        for muscle in e["primaryMuscles"]: grouped.setdefault(muscle, []).append(e)
    lines = ['<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>GymClues exercise library</title><style>:root{color-scheme:dark;background:#0e1319;color:#edf2f7;font:15px system-ui}body{max-width:1000px;margin:auto;padding:28px}h1{letter-spacing:-1px}h2{color:#7ce4ae;margin-top:36px}details{background:#19222c;border:1px solid #2d3a47;border-radius:14px;padding:16px;margin:10px 0}summary{cursor:pointer;font-weight:600}p,li{line-height:1.6;color:#b7c5d3}h3{font-size:15px}a{color:#91b5d9}.pill{font-size:12px;color:#7ce4ae}</style><h1>Exercise library by primary muscle</h1>',
             f'<p>{len(generated)} new full pages; {len(raw_names)} program-name spellings now resolve to library guides. Existing catalog IDs and program prescriptions are preserved.</p>']
    for muscle, rows in sorted(grouped.items()):
        lines.append(f'<h2>{html.escape(muscle)} · {len(rows)}</h2>')
        for e in sorted(rows, key=lambda e: e["name"].lower()):
            lines.append(f'<details><summary>{html.escape(e["name"])}</summary><p class="pill">{html.escape(" · ".join(e["primaryMuscles"]))}</p>')
            if e.get("equipmentNotes"): lines.append('<p>'+html.escape(e["equipmentNotes"])+"</p>")
            for title, field in (("Setup", "setup"), ("How to do it", "steps"), ("Safety tips", "safetyTips"), ("Injury prevention", "injuryPrevention"), ("Common mistakes & corrections", "mistakes")):
                rows2 = e.get(field, [])
                if rows2: lines.append(f'<h3>{title}</h3><ol>'+"".join('<li>'+html.escape(s)+'</li>' for s in rows2)+'</ol>')
            if e.get("breathing"): lines.append('<p>Breathing: '+html.escape(e["breathing"])+"</p>")
            if e.get("choices"): lines.append('<p>Options: '+html.escape(", ".join(c["name"] for c in e["choices"]))+'</p>')
            lines.append('</details>')
    lines.append('</html>')
    (preview / "index.html").write_text("\n".join(lines), encoding="utf-8")
    print(f"Added {len(generated)} pages. Total catalog: {len(all_exercises)} exercises across {len(grouped)} primary groups. All {len(raw_names)} program names resolve.")


if __name__ == "__main__":
    main()
