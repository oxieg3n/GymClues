"""Exercise-specific setup positions for every catalog equipment relationship."""
from build_muscle_maps import key

CABLE = {
    "Cable curl": ("Low pulley; face the station with elbows beside the torso.", "Straight or EZ-style cable bar"),
    "Behind-body cable curl": ("Low pulley; face away and take a short step forward, with the working arm slightly behind the torso.", "Single D-handle"),
    "Overhead cable triceps extension": ("High pulley; face away and take a stable split stance so the rope can pass overhead without pulling the back into an arch.", "Rope"),
    "Cable rope pushdown": ("High pulley; face the station with upper arms close to the sides.", "Rope"),
    "Straight-bar cable pushdown": ("High pulley; face the station and keep the elbows beside the torso.", "Straight bar"),
    "Single-arm cable pushdown": ("High pulley; stand facing the station with the working elbow supported near the side.", "Single handle"),
    "Cable triceps kickback": ("Low pulley; hinge forward with a stable stance and hold the upper arm alongside the torso.", "Single handle"),
    "Cable wrist curl": ("Low pulley; support the forearms on a stable surface or thighs with the wrists free to move.", "Straight bar; palms up"),
    "Cable reverse wrist curl": ("Low pulley; support the forearms and let the wrists move without lifting the elbows.", "Straight bar; palms down"),
    "Cable glute kickback": ("Low pulley; face the tower, hold a fixed hand support and keep the cable clear of the stance foot.", "Secure ankle cuff"),
    "Cable pull-through": ("Low pulley; face away with enough space for the rope to travel between the legs without rubbing the frame.", "Rope"),
    "Standing cable hip abduction": ("Low pulley; stand sideways with a fixed hand support and the cuff on the leg farther from the station.", "Secure ankle cuff"),
    "Seated cable row": ("Low rowing pulley; use the designated foot braces and a stable seat, reaching the handle without rounding the torso.", "Neutral rowing handle or the prescribed row grip"),
    "Cable face pull": ("Pulley near face height; take a stable stance and keep a clear line from the rope to the face.", "Rope"),
    "Cable lateral raise": ("Low pulley; stand sideways so the handle can rise out to the side through a comfortable arc.", "Single D-handle"),
    "Cable rear-delt fly": ("Pulleys around shoulder height; set both sides equally and keep crossed cables running freely.", "Two single handles"),
    "Cable front raise": ("Low pulley; face away or use the prescribed facing position with room for the cable to rise in front of the body.", "Rope, bar or single handle as prescribed"),
    "Standing cable chest fly": ("Pulleys approximately chest-to-shoulder height; match both sides and use a balanced split stance between the towers.", "Two single handles"),
    "Low-to-high cable fly": ("Low pulleys; match both sides and use a stable stance with a clear upward-and-inward line of pull.", "Two single handles"),
    "Kneeling cable crunch": ("High pulley; kneel on a suitable pad far enough away for the rope to clear the head and neck.", "Rope held beside the head; do not pull on the neck"),
    "Standing cable Pallof press": ("Pulley at mid-chest height; stand sideways with the handle initially against the chest.", "Single handle held with both hands"),
    "Standing cable crunch": ("High pulley; face away or use the prescribed position, with stable feet and enough room to curl the trunk without cable contact.", "Rope"),
    "Seated cable crunch": ("High pulley; use a stable, locked seat in the prescribed direction with feet supported and the rope clear of the head.", "Rope"),
    "High-to-low cable chop": ("High pulley; stand diagonally to the tower with room for the hands to travel down and across the body.", "Single handle or rope held with both hands"),
    "Low-to-high cable lift": ("Low pulley; stand diagonally to the tower with room for a controlled upward-and-across path.", "Single handle or rope held with both hands"),
    "Cable side bend": ("Low pulley; stand sideways with the handle at one side and a controlled trunk position.", "Single handle"),
    "Cable Pallof hold": ("Pulley at mid-chest height; stand sideways and choose a load you can hold without rotating toward the tower.", "Single handle held with both hands"),
}

BENCH = {
    "Dumbbell Incline Press": ("Low incline with the seat adjusted to prevent sliding; upper back supported and feet stable.", "Low incline; use the prescribed angle"),
    "Incline dumbbell press": ("Low incline; set the seat so you do not slide and keep the upper back supported.", "Low incline"),
    "Incline dumbbell curl": ("Supported incline sitting position; choose an angle that allows the arms to hang without shoulder strain.", "Comfortable moderate incline"),
    "Dumbbell preacher curl": ("Use a purpose-built preacher pad, or only a bench explicitly suitable for arm support; ensure it cannot tip under one-arm loading.", "Stable upper-arm support; follow equipment instructions"),
    "Concentration curl": ("Sit on a stable seat with feet planted and support the working upper arm against the inner thigh.", "Flat or suitable seated position"),
    "Seated overhead dumbbell extension": ("Sit with feet planted and use a locked upright back pad if prescribed; allow room to bring the dumbbell into and out of position.", "Upright supported position"),
    "Lying EZ-bar triceps extension": ("Center your body on a locked flat bench and leave a clear path to collect and return the bar.", "Flat"),
    "Close-grip Smith bench press": ("Center the locked bench under the Smith rail path; test hook access and stop height with a light bar.", "Flat"),
    "Seated dumbbell wrist curl": ("Sit with feet stable and forearms supported on the thighs or appropriate support, wrists just beyond its edge.", "Flat seated support"),
    "Seated dumbbell reverse wrist curl": ("Sit with forearms supported, palms down and wrists free beyond the edge of the support.", "Flat seated support"),
    "Smith hip thrust": ("Use a bench permitted for hip-thrust support and secured against sliding; upper back rests at its edge, clear of neck pressure.", "Stable flat support"),
    "Seated dumbbell calf raise": ("Sit on a stable seat with knees bent; pad the dumbbells on the lower thighs above, not on, the kneecaps.", "Flat seated position"),
    "Single-leg seated dumbbell calf raise": ("Sit securely and support the dumbbell on the working thigh above the knee; keep the forefoot planted.", "Flat seated position"),
    "Chest-supported dumbbell row": ("Lie chest-down on the locked incline pad with feet stable and room for the dumbbells to hang without hitting the floor.", "Low-to-moderate incline suited to your reach"),
    "One-arm dumbbell row": ("Use the bench for a stable hand or hand-and-knee support without tipping it; leave a clear dumbbell path beside the torso.", "Flat"),
    "Incline dumbbell Y-raise": ("Lie chest-down on a locked incline pad, feet stable, with clear space for the arms to reach into a comfortable Y.", "Low-to-moderate incline"),
    "Seated dumbbell shoulder press": ("Lock the upright back support and sit with feet stable; do not force the lower back into an arch.", "Upright or slightly reclined as prescribed"),
    "Smith overhead press": ("Center a locked supported bench under the rail path and test the bar path, hooks and stops before loading.", "Upright or slightly reclined as prescribed"),
    "Flat dumbbell bench press": ("Lock the bench flat; support the head and upper back with feet planted and space to return the dumbbells safely.", "Flat"),
    "Smith flat bench press": ("Center the flat bench under the rail path and test that both hooks and safety stops can be used without body entrapment.", "Flat"),
    "Smith incline bench press": ("Center the locked incline bench under the rail path and test the bottom position, hook access and safety stops with a light bar.", "Low incline as prescribed"),
    "Seated cable crunch": ("Use a stable, locked seat with feet supported and space between the bench, cable and weight stack.", "Flat seated support unless otherwise prescribed"),
    "Smith Machine Bench Press": ("Center a locked flat bench under the rail path; rehearse unracking and set both safety stops before loading.", "Flat"),
}


def setups_for(e, guides):
    result = []
    cable = {key(n): v for n, v in CABLE.items()}
    bench = {key(n): v for n, v in BENCH.items()}
    ids = list(e["equipmentIds"])
    if e["id"] == "ex_smith_bench" and "eq_adjustable_bench" not in ids:
        ids.append("eq_adjustable_bench")
    for id in ids:
        g = guides[id]
        data = dict(equipmentId=id, position=g["steps"][0]["text"], angle="", attachment="",
                    notes=[g["steps"][1]["text"], "Lock adjustments and rehearse the exercise with light resistance before a working set."])
        if id == "eq_x_cable":
            assert key(e["name"]) in cable, f"Missing cable height / attachment: {e['name']}"
            data["position"], data["attachment"] = cable[key(e["name"])]
            data["notes"] = ["Lock the pulley carriage and close the attachment clip before taking tension.",
                             "Select resistance only with the stack resting; check the cable path through the full intended range."]
        elif id == "eq_adjustable_bench":
            assert key(e["name"]) in bench, f"Missing bench angle: {e['name']}"
            data["position"], data["angle"] = bench[key(e["name"])]
            data["notes"] = ["Fully engage the back-pad and seat locks and check stability before adding load.",
                             "Respect the bench's total user-and-load limit; keep fingers away from adjustment hinges."]
        elif id == "eq_dumbbells":
            data["position"] = "Choose a matched pair or the single dumbbell prescribed. Set a clear pickup and return area."
            data["notes"] = ["Inspect fixed weights or verify every adjustable plate lock before lifting.",
                             "Choose a load you can position and set down without losing control."]
        elif id == "eq_smith":
            data["notes"] = ["Set both safety stops equally for this exercise's bottom position and test them with a light bar.",
                             "Rehearse the hook release and re-rack; center your stance or bench under the actual rail path."]
        if key(e["name"]) == key("Reverse pec deck") and id == "eq_x_pec_deck_machine":
            data["position"] = "Select and lock reverse-fly mode, facing the chest-support pad. Set seat height so the handles align with a comfortable shoulder-level arm path."
        result.append(data)
    return result
