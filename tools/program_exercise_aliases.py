"""Explicit equivalent names. Different equipment/technique remains a separate movement."""
import re


def norm(name):
    return re.sub(r"[^a-z0-9]", "", name.lower())


ALIASES = {}


def aliases(canonical, names):
    for name in [canonical, *names.split("|")]:
        if name.strip():
            k = norm(name.strip())
            assert k not in ALIASES or ALIASES[k] == canonical, name
            ALIASES[k] = canonical


aliases("Barbell bench press", "Bench press|Flat barbell bench press|Flat barbell bench press - back-off")
aliases("Flat dumbbell bench press", "Dumbbell Bench|Dumbbell Bench Press")
aliases("Barbell incline bench press", "Incline Barbell Bench Press")
aliases("Incline dumbbell press", "Incline Dumbbell Bench Press")
aliases("Dumbbell floor press", "Floor Dumbbell Press")
aliases("Barbell back squat", "Back Squat|Barbell Squat|Barbell back squat - back-off")
aliases("Barbell front squat", "Front Squat|Barbell front squat - back-off")
aliases("Squat", "Squats")
aliases("Deadlift", "Deadlift (optional)|Barbell conventional deadlift|Barbell conventional deadlift - back-off")
aliases("Barbell snatch-grip deadlift", "Barbell snatch-grip deadlift - back-off")
aliases("Standing overhead press", "Standing overhead press - back-off|Military Press|Overhead Press")
aliases("Dumbbell stiff-leg deadlift", "Dumbbell Stiff Leg Deadlifts|Dumbbell Stiff Legged Deadlift")
aliases("Stiff-leg deadlift", "Stiff Leg Deadlifts")
aliases("Barbell row", "Barbell Bent Over Row|Bent-Over Barbell Row")
aliases("Bent-over dumbbell row", "Bent Over Dumbbell Row")
aliases("One-arm dumbbell row", "Dumbbell Row|One Arm Dumbbell Row|Single-Arm Dumbbell Row")
aliases("Chest-supported dumbbell row", "Dumbbell Incline Bench Row")
aliases("Seated cable row", "Cable Seated Row")
aliases("Lat pulldown", "Lat Pull Down|Cable Lat Pulldown|Machine Lat Pulldown|Pulldown")
aliases("Lat pulldown (neutral grip)", "V-Bar Lat Pull Down")
aliases("Wide-grip lat pulldown", "Cable Lat Pulldown (Wide Grip)")
aliases("Pull-up", "Pull Up|Pullup")
aliases("Chin-up", "Chin Up|Chin-Ups")
aliases("Weighted or bodyweight pull-up", "")
aliases("Wide-grip pull-up", "Wide Grip Pull-Ups")
aliases("Straight-arm cable pulldown", "Cable Straight Arm Lat Pulldown|Straight Arm Pressdown")
aliases("Cable face pull", "Face pull|Cable Face Pulls|Cable Rope Face Pull")
aliases("Resistance-band pull-apart", "Banded pull-apart")
aliases("Dumbbell rear-delt fly", "Dumbbell Rear Delt Fly|Rear Delt Reverse Fly|Bent-Over Rear Lateral Raise")
aliases("Reverse pec deck", "Reverse Pec Dec|Reverse machine fly|Rear-delt machine fly|Seated machine reverse fly")
aliases("Pec deck fly", "Machine Pec Deck|Pec Dec|Machine fly|Seated machine fly")
aliases("Dumbbell chest fly", "Dumbbell Flys|Flat Bench Dumbbell Flye")
aliases("Floor dumbbell fly", "Floor Dumbbell Fly")
aliases("Standing cable chest fly", "Standing cable crossover")
aliases("Dumbbell pullover", "Dumbbell Pullovers")
aliases("Barbell curl", "Barbell Bicep Curl|Barbell biceps curl|Standing Barbell Curl")
aliases("Standing dumbbell curl", "Dumbbell Bicep Curl|Dumbbell curl|Dumbbell Curls")
aliases("Alternating dumbbell curl", "Alternating Dumbbell Curls")
aliases("Cable curl", "Cable Bicep Curl|Cable Curls")
aliases("EZ-bar curl", "EZ Bar Curls")
aliases("Concentration curl", "Concentration Curls")
aliases("Seated dumbbell curl", "Dumbbell Seated Bicep Curl")
aliases("Incline dumbbell curl", "Seated Incline Dumbbell Curl")
aliases("Dumbbell hammer curl", "Dumbbell Hammer Curls")
aliases("Cable hammer curl", "Cable Rope Hammer Curl")
aliases("Spider curl", "Dumbbell Spider Curl")
aliases("Skullcrusher", "Skull Crushers|Skullcrushers|EZ-bar skullcrusher")
aliases("Overhead cable triceps extension", "Cable overhead triceps extension|Cable Rope Overhead Tricep Extension")
aliases("Seated overhead dumbbell extension", "Seated Dumbbell Extension")
aliases("Dumbbell triceps extension", "Dumbbell Tricep Extension")
aliases("Cable triceps extension", "Cable Tricep Extension|Cable Tricep Extensions|Single-Arm Cable Triceps Extension")
aliases("Cable rope pushdown", "Triceps Rope Pushdown|Rope Extension")
aliases("Triceps pushdown", "Tricep Pushdown|Triceps Pressdown|Standing triceps pushdown")
aliases("Dumbbell triceps kickback", "Triceps Kickbacks|One Arm Kickback")
aliases("Close-grip bench press", "Close Grip Bench Press")
aliases("Close-grip push-up", "Close Grip Push Up")
aliases("Push-up", "Push Up")
aliases("Bodyweight dip", "Bar dip|Dip|Triceps dip")
aliases("Assisted dip", "Assisted bar dip")
aliases("Dumbbell lateral raise", "Dumbbell Side Lateral Raise")
aliases("Cable lateral raise", "Cable Deltoid Raise|Standing cable lateral raise|Cable One-Arm Lateral Raise")
aliases("Lateral raise", "Lateral Raises|Side Lateral Raise|Lateral raise (superset 1)|Lateral raise (superset 2)")
aliases("Seated dumbbell lateral raise", "Seated Lateral Raise")
aliases("Seated dumbbell shoulder press", "Dumbbell Seated Shoulder Press|Seated Dumbbell Press|Seated Overhead Dumbbell Press")
aliases("Machine shoulder press", "Machine Seated Shoulder Press")
aliases("Smith overhead press", "Smith Machine Shoulder Press")
aliases("Dumbbell shrug", "Dumbbell Shrugs|Dumbbell Shoulder Shrug")
aliases("Shrug", "Shrugs|Shoulder shrug")
aliases("Back extension", "Hyperextension|Bodyweight low-back extension")
aliases("Barbell good morning", "Good Morning|Good Mornings")
aliases("Dumbbell reverse lunge", "Dumbbell Rear Lunge")
aliases("Dumbbell step-up", "Dumbbell Step Up")
aliases("Walking lunge", "Walking Lunge")
aliases("Dumbbell split squat", "Dumbbell Split Squat")
aliases("Smith squat", "Smith Machine Squat")
aliases("Leg press", "Machine Leg Press")
aliases("Leg extension", "Leg Extensions|Machine Leg Extension|Seated leg extension")
aliases("Seated leg curl", "Machine Seated Leg Curl")
aliases("Lying leg curl", "Machine Leg Curl (Prone)|Machine Lying Leg Curl")
aliases("Hip abduction machine", "Machine Hip Abduction")
aliases("Cable pull-through", "Cable Pull Through with Rope")
aliases("Leg press calf raise", "Calf Press On Leg Press|Calf Press")
aliases("Standing calf raise", "Standing Calf Raise")
aliases("Standing dumbbell calf raise", "Dumbbell Standing Calf Raise")
aliases("Seated dumbbell calf raise", "Seated Dumbbell Calf Raise")
aliases("Standing calf raise machine", "Machine calf raise")
aliases("Smith standing calf raise", "Smith machine calf raise")
aliases("Machine triceps extension", "Machine Tricep Extension")
aliases("Machine chest press", "Machine bench press")
aliases("Incline machine chest press", "Machine Incline Chest Press")
aliases("Crunch", "Crunches")
aliases("Hanging leg raise", "Hanging Leg Raises")
aliases("Cable crunch", "Cable Crunch")
aliases("Seated abdominal crunch machine", "Machine crunch")
aliases("Bicycle crunch", "Bicycle Crunches")
aliases("Wrist curl", "Wrist Curls")
aliases("Reverse wrist curl", "Reverse Wrist Curls")

# An offered choice has a page explaining the prescription and linking each option.
CHOICES = {
    "Pull Ups or Machine Rows": ["Pull-up", "Machine row"],
    "Pulldown or pull-up or chin-up": ["Lat pulldown", "Pull-up", "Chin-up"],
    "Pull-up or pulldown": ["Pull-up", "Lat pulldown"],
    "Pull-up (self or assisted)": ["Pull-up", "Assisted pull-up"],
    "Seated cable row or chest-supported row": ["Seated cable row", "Chest-supported dumbbell row"],
    "Calf raise (seated or standing)": ["Seated calf raise", "Standing calf raise"],
    "Calf Exercise": ["Seated calf raise", "Standing calf raise"],
    "Leg curl (seated or lying)": ["Seated leg curl", "Lying leg curl"],
    "Incline bench press (dumbbell or barbell)": ["Incline dumbbell press", "Barbell incline bench press"],
    "One-arm row (dumbbell or barbell)": ["One-arm dumbbell row", "One-arm barbell row"],
    "Shrug (dumbbell, barbell or trap bar)": ["Dumbbell shrug", "Barbell shrug", "Trap-bar shrug"],
    "Seated triceps extension (DB, rope or EZ-bar)": ["Seated overhead dumbbell extension", "Seated rope overhead triceps extension", "Seated EZ-bar triceps extension"],
}
