"""Generate the offline setup and exercise-safety supplement without changing workbook assets.

Run: python tools/build_training_guides.py
Unknown exercises/equipment fail generation rather than getting a guessed guide.
"""
import json
from pathlib import Path
from build_muscle_maps import ASSIGNMENTS, PROFILES as MUSCLE_PROFILES, key
from equipment_setups import GUIDES, vector, svg
from training_safety import PROFILES, CATALOG_ALIASES
from exercise_configurations import setups_for

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main"
SOURCES = [
    dict(title="Weight-training technique · Mayo Clinic", url="https://www.mayoclinic.org/healthy-lifestyle/fitness/in-depth/weight-training/art-20045842"),
    dict(title="Exercise library · ACE", url="https://www.acefitness.org/resources/everyone/exercise-library/"),
    dict(title="Machine manuals · Life Fitness", url="https://support.lifefitness.com/hc/en-us/p/Manuals-Resources"),
    dict(title="Knee-pivot alignment · Precor", url="https://www.precor.com/en-US/products/VSL005BP"),
]


def equipment_for(name, profile):
    """Conservative, build-time links for clearly named equipment; no runtime inference."""
    n = name.lower()
    found = []
    for word, id in (("smith", "eq_smith"), ("dumbbell", "eq_dumbbells"), ("ez-bar", "eq_x_ez_bar"),
                     ("ez bar", "eq_x_ez_bar"), ("cable", "eq_x_cable"), ("barbell", "eq_x_barbell")):
        if word in n and id not in found:
            found.append(id)
    if not found:
        exact = {
            "deadlift": ["eq_x_barbell"], "bench press": ["eq_x_barbell", "eq_adjustable_bench"],
            "lat pulldown": ["eq_lat_pulldown"], "leg press": ["eq_leg_press"],
            "leg extension": ["eq_x_leg_extension_machine"], "leg extensions": ["eq_x_leg_extension_machine"],
            "seated leg curl": ["eq_x_seated_leg_curl_machine"], "lying leg curl": ["eq_x_lying_leg_curl_machine"],
            "hack squat": ["eq_x_hack_squat_machine"], "machine preacher curl": ["eq_x_preacher_curl_machine"],
        }
        found = exact.get(n, [])
    if "eq_dumbbells" in found and profile in {"bench", "incline", "decline", "seated_press", "supported_row", "incline_fly"} and "floor" not in n:
        found.append("eq_adjustable_bench")
    return list(dict.fromkeys(found))


def main():
    assets = [json.loads((MAIN / f"assets/{n}.json").read_text(encoding="utf-8")) for n in ("content", "library")]
    catalog = [e for a in assets for e in a["exercises"]]
    equipment = {q["id"]: q for a in assets for q in a["equipment"]}
    programs = json.loads((MAIN / "assets/programs.json").read_text(encoding="utf-8"))["programs"]
    names = {i["exercise"] for p in programs for d in p["days"] for i in d["items"]} | {e["name"] for e in catalog}
    assert GUIDES.keys() == equipment.keys(), f"Equipment coverage mismatch: {GUIDES.keys() ^ equipment.keys()}"
    assert MUSCLE_PROFILES.keys() <= PROFILES.keys(), f"Missing movement safety: {MUSCLE_PROFILES.keys() - PROFILES.keys()}"
    aliases = {key(n): p for n, p in CATALOG_ALIASES.items()}
    profiles = dict(PROFILES)
    profiles["general"] = dict(
        safetyTips=["Choose a load and setup you can control; stop for sharp pain, numbness or dizziness.",
                    "Check the equipment and keep the movement area clear before starting."],
        injuryPrevention=["Rehearse the movement with little or no resistance before increasing difficulty.",
                          "For an unfamiliar exercise or an existing injury, get appropriate individual instruction."],
        mistakes=["Using momentum to move an uncontrolled load: reduce resistance and restore control.",
                  "Forcing range through joint pain: stop and reassess the movement.",
                  "Continuing after technique deteriorates: end the set and recover."])
    by_name = {}
    for e in catalog:
        by_name.setdefault(key(e["name"]), []).extend(s["equipmentId"] for s in setups_for(e, GUIDES))
    exercises = {}
    for name in sorted(names):
        n = key(name)
        variants = [dict(label="", profile=aliases[n])] if n in aliases else ASSIGNMENTS.get(n)
        assert variants, f"Missing safety guide: {name}"
        result = []
        for v in variants:
            assert v["profile"] in profiles, (name, v)
            ids = list(dict.fromkeys(by_name.get(n, equipment_for(name, v["profile"]))))
            assert set(ids) <= equipment.keys(), (name, ids)
            result.append(dict(**v, equipmentIds=ids))
        exercises[n] = result
    for id, p in profiles.items():
        assert len(p["safetyTips"]) >= 2 and len(p["injuryPrevention"]) >= 2 and len(p["mistakes"]) >= 3, id
        assert all(":" in m for m in p["mistakes"]), f"Mistake without correction: {id}"
    output_eq = {}
    preview = ROOT / "docs/training-guides"
    preview.mkdir(parents=True, exist_ok=True)
    for id, g in GUIDES.items():
        image = "setup_" + id.removeprefix("eq_")
        (MAIN / f"res/drawable/{image}.xml").write_text(vector(g), encoding="utf-8")
        (preview / f"{image}.svg").write_text(svg(g), encoding="utf-8")
        output_eq[id] = dict(name=equipment[id]["name"], image=image, steps=g["steps"], caution=g["caution"])
    setups = {e["id"]: setups_for(e, GUIDES) for e in catalog}
    library = dict(equipment=output_eq, profiles=profiles, exercises=exercises, exerciseSetups=setups, sources=SOURCES,
        equipmentCheck="Before adjusting, let the load rest, keep hands clear of moving parts, and check the machine's placard. Lock every adjustment and test with a light load. Report damaged cables, pads or pins to gym staff.",
        stopRule="Stop for sharp pain, joint pain, numbness or dizziness. Use qualified instruction for unfamiliar movements and individualized advice for an existing injury.")
    (MAIN / "assets/training_guides.json").write_text(json.dumps(library, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    # The browser review page and native UI consume the same data and artwork.
    data = json.dumps(library, ensure_ascii=False).replace("</", "<\\/")
    names_json = json.dumps(sorted(names), ensure_ascii=False).replace("</", "<\\/")
    (preview / "index.html").write_text('''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>GymClues · Setup and safety guides</title>
<style>:root{color-scheme:dark;font:15px system-ui;background:#0e1319;color:#edf2f7}*{box-sizing:border-box}body{margin:0;padding:28px}main{max-width:960px;margin:auto}h1{letter-spacing:-1px;font-size:32px}h2{margin-top:32px}p,li{line-height:1.6;color:#b7c5d3}select,button{font:inherit;background:#19222c;color:#edf2f7;border:1px solid #465462;padding:12px;border-radius:12px;max-width:100%;margin:6px 6px 6px 0}img{width:100%;border-radius:18px;background:#111a23}.card{background:#19222c;border:1px solid #2d3a47;border-radius:18px;padding:20px;margin:12px 0}h3{color:#7ce4ae;font-size:16px}a{color:#91b5d9}.number{color:#ffd18a;margin-right:10px}small{color:#aab7c5}</style>
<main><small>GYMCLUES / TRAIN SMARTER</small><h1>Equipment setup &amp; exercise safety</h1><p id="coverage"></p><h2>Equipment setup diagrams</h2><label for="equipment">Equipment</label><br><select id="equipment"></select><div class="card"><h3 id="equipmentName"></h3><img id="diagram" alt="Numbered equipment configuration diagram"><p>Mint: equipment contact surfaces · Blue: body / adjustment direction · Gold: locks, pivots and numbered callouts</p><p id="checks"></p><div id="steps"></div><p id="caution"></p><small>Schematic, not a model-specific service drawing. Follow the controls, limits and instructions on your machine.</small></div>
<h2>Exercise safety &amp; common mistakes</h2><label for="exercise">Exercise</label><br><select id="exercise"></select><div id="variants"></div><div id="safety"></div><p id="stop"></p><p>Original coaching summaries for typical technique. These do not guarantee injury prevention or replace individual instruction. Sources provide general context; each entry is not independently certified by those organizations.</p><div id="sources"></div></main>
<script>const data=DATA,names=NAMES,$=id=>document.getElementById(id),key=n=>n.toLowerCase().replace(/[^a-z0-9]+/g,'_').replace(/^_|_$/g,'');
$('coverage').textContent=Object.keys(data.equipment).length+' equipment guides and '+names.length+' exercise-name spellings, covering every bundled catalog exercise and all 21 programs.';
Object.entries(data.equipment).forEach(([id,q])=>{let o=document.createElement('option');o.value=id;o.textContent=q.name;$('equipment').append(o)});
function setup(){let q=data.equipment[$('equipment').value];$('equipmentName').textContent=q.name;$('diagram').src=q.image+'.svg';$('diagram').alt='Numbered setup diagram for '+q.name;$('checks').textContent=data.equipmentCheck;$('caution').textContent=q.caution;$('steps').replaceChildren();q.steps.forEach((s,i)=>{let h=document.createElement('h3');h.textContent=(i+1)+'. '+s.title;let p=document.createElement('p');p.textContent=s.text;$('steps').append(h,p)})};$('equipment').onchange=setup;setup();
names.forEach(n=>{let o=document.createElement('option');o.textContent=n;$('exercise').append(o)});
function show(i){let v=data.exercises[key($('exercise').value)][i],p=data.profiles[v.profile];$('safety').replaceChildren();[['Safety tips',p.safetyTips],['Injury prevention',p.injuryPrevention],['Common mistakes & corrections',p.mistakes]].forEach(([title,rows])=>{let box=document.createElement('section');box.className='card';let h=document.createElement('h3');h.textContent=title;let ul=document.createElement('ul');rows.forEach(t=>{let li=document.createElement('li');li.textContent=t;ul.append(li)});box.append(h,ul);$('safety').append(box)});v.equipmentIds.forEach(id=>{let b=document.createElement('button');b.textContent='Setup: '+data.equipment[id].name;b.onclick=()=>{$('equipment').value=id;setup();$('equipment').scrollIntoView({behavior:'smooth'})};$('safety').append(b)})}
function select(){let list=data.exercises[key($('exercise').value)];$('variants').replaceChildren();if(list.length>1)list.forEach((v,i)=>{let b=document.createElement('button');b.textContent=v.label;b.onclick=()=>show(i);$('variants').append(b)});show(0)};$('exercise').onchange=select;select();$('stop').textContent=data.stopRule;data.sources.forEach(s=>{let p=document.createElement('p'),a=document.createElement('a');a.href=s.url;a.textContent=s.title;a.target='_blank';a.rel='noreferrer';p.append(a);$('sources').append(p)});
</script></html>'''.replace("DATA", data).replace("NAMES", names_json), encoding="utf-8")
    print(f"Generated {len(output_eq)} setup images and {len(exercises)} safety entries for {len(catalog)} catalog exercises and all program prescriptions ({len(profiles)} movement profiles).")


if __name__ == "__main__":
    main()
