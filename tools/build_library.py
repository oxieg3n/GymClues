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
 "P3": "A high-volume powerbuilding Push/Pull/Legs plan on an 8-day cycle (Push A, Pull A, Legs A, rest, Push B, Pull B, Legs B, rest), so you train six times in eight days, not six times every calendar week. Each day has a main lift for 5 sets with a 15 total-rep goal, a lighter AMQRAP back-off set, then short-rest accessory work with total-rep goals (such as 50 across 5 sets). Rep targets are totals across sets, not per set. It is the most demanding option here and suits intermediate and advanced lifters."}
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
        sourceUrl=S(c["Source URL"]), description=DESC[pid], cycleLength=len(days), days=days, notes=instr.get(pid, [])))
json.dump(dict(programs=progs), open(OUT + "programs.json", "w", encoding="utf-8"), indent=1, ensure_ascii=False)
print(len(progs), "programs,", sum(len(d["items"]) for p in progs for d in p["days"]), "rows")
