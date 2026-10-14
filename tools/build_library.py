"""Spreadsheets -> app assets.
Usage: python tools/build_library.py top_10_exercises_by_muscle_group.xlsx published_bro_and_ppl_workout_programs.xlsx
Writes app/src/main/assets/library.json (extra equipment + exercises) and programs.json.
Original program prescriptions are copied verbatim as text; substitutions stay in a separate field."""
import sys, re, json, openpyxl
ex_f, pr_f = sys.argv[1], sys.argv[2]
S = lambda v: "" if v is None else str(v).strip()
slug = lambda s: re.sub(r"[^a-z0-9]+", "_", s.lower()).strip("_")
OUT = "app/src/main/assets/"

# ---------- exercises ----------
EXISTING = {"dumbbell": "eq_dumbbells", "bench": "eq_adjustable_bench", "smith": "eq_smith", "leg press": "eq_leg_press",
            "chest press machine": "eq_chest_press", "lat pulldown": "eq_lat_pulldown"}
NEW_EQ = {"eq_x_cable": ("Cable Station", "Cable"), "eq_x_barbell": ("Barbell and Rack", "Free weight"),
          "eq_x_ez_bar": ("EZ Bar", "Free weight"), "eq_x_trap_bar": ("Trap Bar", "Free weight")}
machines = {}
def equip(part):
    p = part.lower().strip()
    if re.search(r"cable|rope|straight|handle|ankle cuff|dual", p) and "machine" not in p: return "eq_x_cable"
    if "smith" in p: return "eq_smith"
    if "dumbbell" in p: return "eq_dumbbells"
    if "bench" in p and "extension" not in p: return "eq_adjustable_bench"
    if p in ("barbell", "rack"): return "eq_x_barbell"
    if "ez" in p and "bar" in p: return "eq_x_ez_bar"
    if "trap bar" in p: return "eq_x_trap_bar"
    if "leg press" in p: return "eq_leg_press"
    if "chest press machine" == p: return "eq_chest_press"
    if "lat pulldown" in p: return "eq_lat_pulldown"
    if "pec deck" in p or "rear-delt" in p: p = "pec deck machine"
    if "machine" in p or "extension bench" in p:
        name = re.sub(r"\s+with .*", "", p).title().replace("45-Degree", "45-Degree")
        machines["eq_x_" + slug(name)] = name; return "eq_x_" + slug(name)
    return None   # clear space, wall, platform, bar pad...: no equipment entry
def muscles(group, target):
    t = target.lower()
    if group == "Thighs":
        return ["Hamstrings"] if "hamstring" in t and "quad" not in t else ["Quads"]
    return {"Upper Back": ["Back"], "Lower Back": ["Back"]}.get(group, [group])
MOVE = {"Chest": "Push", "Shoulders": "Push", "Triceps": "Push", "Back": "Pull", "Biceps": "Pull", "Forearms": "Pull",
        "Quads": "Legs", "Hamstrings": "Legs", "Glutes": "Legs", "Calves": "Legs", "Core": "Core"}
ws = openpyxl.load_workbook(ex_f, data_only=True)["All Exercises"]
exs = {}
for r in list(ws.iter_rows(min_row=2, values_only=True)):
    g, rank, name, eq, target, role, why, cue, ids, url, caution = [S(c) for c in r[:11]]
    if not name: continue
    e = exs.setdefault(name, dict(id="ex_x_" + slug(name), name=name, equipmentIds=[], primaryMuscles=[], secondaryMuscles=[],
        movement="", difficulty="Beginner", steps=[], safety="", target=target, why=why, source=url))
    for m in muscles(g, target):
        if m not in e["primaryMuscles"]: e["primaryMuscles"].append(m)
    for part in re.split(r"\s*\+\s*", eq):
        i = equip(part)
        if i and i not in e["equipmentIds"]: e["equipmentIds"].append(i)
    if cue and cue not in e["steps"]: e["steps"].append(cue)
    for c in re.split(r"(?<=\.)\s+", caution):
        if c and c not in e["safety"]: e["safety"] = (e["safety"] + " " + c).strip()
    e["movement"] = e["movement"] or MOVE[e["primaryMuscles"][0]]
equipment = [dict(id=i, name=n, category=c, notes="Added from the exercise library import.") for i, (n, c) in NEW_EQ.items()] + \
            [dict(id=i, name=n, category="Selectorized", notes="Added from the exercise library import.") for i, n in sorted(machines.items())]
json.dump(dict(equipment=equipment, exercises=list(exs.values())), open(OUT + "library.json", "w", encoding="utf-8"), indent=1, ensure_ascii=False)
print(len(exs), "exercises,", len(equipment), "new equipment")

# ---------- programs ----------
wb = openpyxl.load_workbook(pr_f, data_only=True)
def sheet(n):
    ws = wb[n]; h = [c.value for c in ws[1]]
    return [dict(zip(h, r)) for r in ws.iter_rows(min_row=2, values_only=True) if r[0]]
DESC = {
 "B1": "Five body-part days (Back, Chest and Abs, Legs, Shoulders and Abs, Arms) followed by two rest days. Each muscle group is hit hard once a week with 3-5 sets per exercise and short rests of 30-45 seconds, so sessions are dense. It starts most days with a heavy 6-rep lift before moving to 8-15 rep work. Versus PPL, each muscle gets one big session a week instead of two. Versus the other bro splits, it has the shortest rests and a high exercise count per day.",
 "B2": "A five-day bro split in the order Chest, Back, Shoulders, Legs, Arms, then two rest days. Each day has four or five exercises, built around one heavy compound lift plus a few isolation moves. Rests are longer (about 2 minutes on compounds, 1 minute on isolation) and several sets are taken to AMRAP or near failure. Versus the Muscle & Strength split, it has fewer exercises per day and longer rests. Versus the Fitness Phantom split, it assumes more training experience. Progress by adding weight once your rep targets are met.",
 "B3": "A five-day split of Chest, Legs, Back, Shoulders and Arms with two rest days, written for newer lifters. It adds explicit warm-up blocks before most sessions and leans on Smith machine, machine and dumbbell lifts with higher reps (12-20) and descending rep ladders such as 15, 12, 10, 10. Versus the other two bro splits, reps are higher and there is more guided warm-up. The source suggests adding 2.5-5 lb weekly or whenever you finish every prescribed rep with good form. Arm day has no warm-up in the source.",
 "P1": "A six-day Push/Pull/Legs routine (Pull A, Push A, Legs A, Pull B, Push B, Legs B, then one rest day) built on linear progression. Each day opens with one heavy barbell lift for 5 reps, with a final AMRAP set on bench, squat, row, overhead press and deadlift, followed by 8-12 rep accessories. You aim to add weight every session, so it suits true novices. Versus the other two PPL programs, it has the most frequent load jumps and AMRAP finishing sets. Versus the bro splits, each muscle is trained twice a week.",
 "P2": "A six-day Push/Pull/Legs cycle (Push 1, Pull 1, Legs 1, Push 2, Pull 2, Legs 2, then rest) using the Lift Vault version of the Coolcicada routine. Main lifts are 3 sets of 5 (squat 4 sets of 5-6) and accessories are 8-12 reps, with no AMRAP sets or total-rep goals, so it is simpler than the other two PPL programs. Both halves of the week repeat the same sessions. Add about 5 lb to compounds after hitting your targets. It fits late beginners and intermediates who can recover from six days a week. Other versions of this routine differ, so stay with this one.",
 "P3": "A high-volume powerbuilding Push/Pull/Legs plan on an 8-day cycle (Push A, Pull A, Legs A, rest, Push B, Pull B, Legs B, rest), so you train six times in eight days, not six times every calendar week. Each day has a main lift for 5 sets with a 15 total-rep goal, a lighter AMQRAP back-off set, then short-rest accessory work with total-rep goals (such as 50 across 5 sets). Rep targets are totals across sets, not per set. It is the most demanding option here and suits intermediate and advanced lifters.",
 "U1": "A four-day Upper/Lower split: Upper Power, Lower Power, rest, Upper Hypertrophy, Lower Hypertrophy, then two rest days. Power days use heavy 3-5 rep sets on bench, squat and rows; hypertrophy days move to 8-12 reps. Versus the other Upper/Lower programs, it is the only one that mixes heavy and high-rep days in one week.",
 "U2": "A four-day Upper/Lower split (Upper A, Lower A, rest, Upper B, Lower B, rest, rest) with 8 exercises per day, mostly 3 sets of 6-12 reps and 60-90 seconds rest. It labels itself beginner-friendly but carries substantial volume. Versus PHUL, every day is moderate-rep bodybuilding work, with no heavy power days.",
 "U3": "A four-day Upper/Lower split that needs only dumbbells: 6-7 exercises a day, mostly 4 sets of 8-10 reps with 30-60 second rests. It is the simplest Upper/Lower option if your gym visits are dumbbell-focused. Versus the other Upper/Lower programs, it uses no barbell, machine or cable lifts.",
 "F1": "A three-exercise, three-day-a-week full-body plan that alternates two workouts, A and B, across a 14-day cycle. Each lift is mostly 5 sets of 5, starting with squats, and the load goes up once all reps are completed. Versus the other full-body programs, it has the fewest exercises and the longest rests (1-5 minutes).",
 "F2": "A three-day full-body plan (Full Body 1, 2 and 3, with rest days between) with 9 exercises per session. Each day leads with a 5x5 main lift (squat, bench or deadlift) followed by accessory work at 10 reps and 45-60 second rests. Versus StrongLifts, it adds far more volume per session and suits intermediates.",
 "F3": "A three-day full-body plan using only dumbbells: 7 exercises per session, mostly 3 sets of 10 with about 60 seconds rest, built from squats, lunges, step-ups and rows. Versus the other full-body programs, it is the lightest on skill and equipment demands and is labelled beginner.",
 "A1": "A six-day Arnold-style split repeated twice a week: Chest and Back, Shoulders and Arms, Legs and Lower Back, then one rest day. Each exercise is 3-4 sets of about 10 reps, with up to 11 exercises on the arm day. Versus the other Arnold programs, it has the highest volume and is labelled advanced.",
 "A2": "A six-day Arnold-style split (Chest and Back, Shoulders and Arms, Legs, repeated, then rest) with 4 sets of 8-12 reps on main lifts and rests of 60-120 seconds. Progression is double progression: reach the top of the rep range on all sets, then add weight. Versus the other Arnold programs, it has the most structured progression rule.",
 "A3": "A six-day Arnold-style split with separate A and B versions of each session and high-rep pyramids such as 30, 12, 10, 8, 6 on the first lifts. It runs 4 weeks plus a deload week at half volume. Versus the other Arnold programs, it is the most pyramid-driven and has no fixed rest times.",
 "T1": "A four-day Torso/Limbs split (Torso A, Limbs A, rest, Torso B, Limbs B, then two rest days) with about 6 exercises per day. Many lifts use a top set followed by a lighter back-off set, and the source mixes several progression methods. Versus the other Torso/Limbs programs, it has the fewest exercises per day.",
 "T2": "A four-day Torso/Limbs split (Torso A, Limbs A, Torso B, Limbs B) with 7 exercises per day, mixing barbell, Smith machine and machine lifts at 6-15 reps. The source gives no progression rule and no rest times, so you choose your own. Versus the other Torso/Limbs programs, it is the most machine-friendly.",
 "T3": "A four-day Torso/Limbs split with a second version of each day (Torso, Limbs, Torso 2.0, Limbs 2.0), at 11-12 entries per session. Many entries are single sets of 12 and some are repeated, as published. Versus the other Torso/Limbs programs, it has the most entries per day.",
 "H1": "A five-day hybrid of Push/Pull/Legs and Upper/Lower: Push, Pull, Legs, rest, Upper, Lower, rest. Each day has only 4 exercises, mostly 3 sets of 4-6 reps with 2-5 minute rests. Versus the other hybrids, it is the shortest per session and the heaviest, and some experience is preferred.",
 "H2": "A five-day hybrid of Push/Pull/Legs and Upper/Lower (Push, Pull, Legs, rest, Upper, Lower, rest) with 5 exercises per day, mostly 3 sets of 6-12 reps. Rests are matched to the rep range, from 1.5 to 3 minutes. Versus Legion's hybrid, it uses a bit more volume at moderate reps and is aimed at intermediate and advanced lifters.",
 "H3": "A six-week, four-day dumbbell hybrid (Pull, Legs and Abs, rest, Push, Full-Body Circuit, then two rest days) built on high-rep sets such as 30, 25, 20, 15, 10. One-sided lifts have the other side work as rest. Versus the other hybrids, it needs only dumbbells and is the most endurance-style.",
}
comp = {r["ID"]: r for r in sheet("Program Comparison")}
sched = {}
for r in sheet("Schedules"): sched.setdefault(r["ID"], []).append(r)
work = {}
for r in sheet("All Workouts"): work.setdefault((r["Program ID"], int(r["Cycle day"])), []).append(r)
instr = {}
for r in sheet("Program Instructions"): instr.setdefault(r["ID"], []).append(dict(topic=S(r["Topic"]), text=S(r["Instruction / source clarification"])))
def restsec(t):
    m = re.search(r"(\d+)(?:-\d+)?\s*(sec|min)", t or "")
    return 90 if not m else int(m.group(1)) * (60 if m.group(2) == "min" else 1)
def setsn(t):
    n = [int(x) for x in re.findall(r"\d+", t or "")]
    return max(1, min(10, sum(n) if "+" in (t or "") else (n[0] if n else 1)))
def weeks(d):   # upper bound of a stated duration; programs with no fixed length are assumed to be 12 weeks
    n = [int(x) for x in re.findall(r"\d+", d)]
    if "+" in d: return sum(n[:2]) if len(n) > 1 else 12
    return max(n) if n and "week" in d else 12
progs = []
for pid, c in comp.items():
    days = []
    for s in sorted(sched[pid], key=lambda s: int(s["Cycle day"])):
        d = int(s["Cycle day"]); rows = sorted(work.get((pid, d), []), key=lambda r: (r["Phase"] != "Warm-up", int(r["Order"])))
        days.append(dict(day=d, session=S(s["Session"]), training=S(s["Status"]) == "Training", items=[dict(
            phase=S(r["Phase"]), exercise=S(r["Original exercise"]), sets=S(r["Sets"]), reps=S(r["Reps / target"]),
            repsNote=S(r["Rep interpretation"]), rest=S(r["Rest (source)"]), restSec=restsec(S(r["Rest (source)"])), setCount=setsn(S(r["Sets"])),
            notes=S(r["Superset / optional / loading notes"]), substitution=S(r["Optional gym substitution - NOT original"])) for r in rows]))
    progs.append(dict(id=pid, name=S(c["Program"]), split=S(c["Split"]), author=S(c["Author"]), level=S(c["Level / suitability"]),
        duration=S(c["Duration"]), schedule=S(c["Schedule"]), progression=S(c["Progression (source summary)"]), rest=S(c["Rest guidance"]),
        sourceUrl=S(c["Source URL"]), description=DESC[pid], totalWeeks=weeks(S(c["Duration"])), cycleLength=len(days), days=days, notes=instr.get(pid, [])))
json.dump(dict(programs=progs), open(OUT + "programs.json", "w", encoding="utf-8"), indent=1, ensure_ascii=False)
print(len(progs), "programs,", sum(len(d["items"]) for p in progs for d in p["days"]), "rows")
