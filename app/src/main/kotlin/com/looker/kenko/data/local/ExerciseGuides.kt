/*
 * Copyright (C) 2026 Kitae Contributors
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.looker.kenko.data.local

// How to do the exercises Kitae ships with
val ExerciseGuides: Map<String, List<String>> = mapOf(
    "Curls" to listOf(
        "Stand tall with a dumbbell in each hand, palms facing forward.",
        "Keep your elbows at your sides and curl the weights up towards your shoulders.",
        "Squeeze your biceps at the top, then lower slowly until your arms are straight.",
        "Don't swing your body to lift the weight.",
    ),
    "Barbell Curls" to listOf(
        "Stand holding a barbell with an underhand grip, hands about shoulder-width apart.",
        "Keeping your elbows tucked at your sides, curl the bar up to your upper chest.",
        "Lower it slowly until your arms are straight.",
        "Keep your back straight and don't lean back to lift.",
    ),
    "Preacher Curls" to listOf(
        "Sit at a preacher bench with the backs of your upper arms flat on the pad.",
        "Hold the bar or dumbbells with an underhand grip and curl up until your forearms are nearly vertical.",
        "Lower slowly until your arms are almost straight.",
        "Keep your upper arms on the pad the whole time.",
    ),
    "Incline Bicep Curls" to listOf(
        "Sit back on a bench set to about 45 degrees, a dumbbell in each hand and arms hanging straight down.",
        "Keeping your upper arms still, curl the dumbbells up towards your shoulders.",
        "Lower slowly until your arms are straight, letting your biceps stretch.",
        "Keep your back and head against the bench.",
    ),
    "Tricep Push Down" to listOf(
        "Stand facing a high cable with a bar or rope, elbows bent and tucked at your sides.",
        "Push down until your arms are straight, keeping your elbows pinned in place.",
        "Squeeze your triceps, then let the handle rise slowly until your forearms are about level with the floor.",
        "Stand tall and don't lean over the handle.",
    ),
    "Skull-Crushers" to listOf(
        "Lie on a flat bench holding a barbell or EZ-bar above your chest with straight arms.",
        "Keeping your upper arms still, bend your elbows to lower the bar towards your forehead.",
        "Stop just before it touches, then straighten your arms again.",
        "Keep your elbows pointing up rather than flaring out.",
    ),
    "Overhead Extensions" to listOf(
        "Hold a dumbbell with both hands, or a cable rope, overhead with straight arms.",
        "Keeping your upper arms close to your head, bend your elbows to lower the weight behind your head.",
        "Straighten your arms again and squeeze your triceps.",
        "Brace your core so your lower back doesn't arch.",
    ),
    "Lateral Raises" to listOf(
        "Stand with a dumbbell in each hand at your sides, elbows slightly bent.",
        "Raise your arms out to the sides until they reach shoulder height.",
        "Pause, then lower slowly.",
        "Lead with your elbows and don't shrug your shoulders up.",
    ),
    "Shoulder Press" to listOf(
        "Sit or stand holding dumbbells at shoulder height, palms facing forward.",
        "Press the weights up until your arms are straight overhead.",
        "Lower them back to shoulder height with control.",
        "Keep your core tight and don't arch your lower back.",
    ),
    "Face Pulls" to listOf(
        "Set a rope on a cable at about face height and hold it with both hands, palms facing in.",
        "Step back until your arms are straight, then pull the rope towards your face, spreading your hands apart.",
        "Finish with your elbows high and out to the sides, squeezing the backs of your shoulders.",
        "Return slowly until your arms are straight.",
    ),
    "Squats" to listOf(
        "Rest the bar across your upper back, feet about shoulder-width apart and toes turned slightly out.",
        "Brace your core and sit down and back, bending your hips and knees together.",
        "Go as low as you can with a straight back and heels down, ideally until your thighs are at least level with the floor.",
        "Drive through your whole foot to stand up, keeping your knees in line with your toes.",
    ),
    "Leg Press" to listOf(
        "Sit in the machine with your back flat against the pad and your feet shoulder-width apart on the platform.",
        "Release the safeties and lower the platform by bending your knees towards your chest.",
        "Go as deep as you can without your lower back lifting off the pad.",
        "Press back up, stopping just short of locking your knees.",
    ),
    "Hack Squats" to listOf(
        "Stand in the machine with your back against the pad, shoulders under the pads and feet shoulder-width apart.",
        "Release the safeties and bend your knees to lower yourself as deep as you comfortably can.",
        "Push through your feet to straighten your legs, stopping just short of locking out.",
        "Keep your back flat against the pad throughout.",
    ),
    "Stiff Legged Deadlift" to listOf(
        "Stand holding a barbell in front of your thighs, feet hip-width apart and knees only slightly bent.",
        "Push your hips back and lower the bar along your legs, keeping your back flat.",
        "Go down until you feel a strong stretch in your hamstrings.",
        "Drive your hips forward to stand up, squeezing your glutes at the top.",
    ),
    "Lying Leg Curls" to listOf(
        "Lie face down on the machine with the pad just above your heels and your knees just past the edge of the bench.",
        "Curl your heels towards your glutes as far as you can.",
        "Pause, then lower slowly until your legs are straight.",
        "Keep your hips pressed into the pad.",
    ),
    "Calf Raises" to listOf(
        "Stand with the balls of your feet on a step or raised edge, heels hanging off.",
        "Rise up onto your toes as high as you can.",
        "Pause, then lower your heels below the step to stretch your calves.",
        "Keep your knees straight but not locked.",
    ),
    "Hip Thrusts" to listOf(
        "Sit on the floor with your upper back against a bench and a barbell or weight across your hips.",
        "Plant your feet flat, about hip-width apart, with your knees bent.",
        "Drive through your heels to lift your hips until your body is straight from shoulders to knees.",
        "Squeeze your glutes at the top, then lower with control. Keep your chin tucked.",
    ),
    "Lunges" to listOf(
        "Stand tall, holding dumbbells at your sides if you want extra weight.",
        "Step forward with one leg and lower until both knees are bent to about 90 degrees.",
        "Push off your front foot to come back to standing.",
        "Switch legs each rep, keeping your torso upright and your front knee over your foot.",
    ),
    "Sit-ups" to listOf(
        "Lie on your back with knees bent and feet flat, hands crossed on your chest or lightly by your ears.",
        "Curl your upper body up towards your knees using your abs.",
        "Lower back down slowly.",
        "Don't pull on your neck with your hands.",
    ),
    "Leg Raises" to listOf(
        "Lie on your back with your legs straight and your hands under your hips or by your sides.",
        "Keeping your legs straight, raise them until they point at the ceiling.",
        "Lower them slowly, stopping just before your heels touch the floor.",
        "Keep your lower back pressed into the floor.",
    ),
    "Bench Press" to listOf(
        "Lie on the bench with your eyes under the bar and your feet flat on the floor.",
        "Grip the bar a little wider than shoulder-width, pull your shoulder blades back and lift it off the rack.",
        "Lower the bar to your mid chest, elbows at about 45 degrees to your body.",
        "Press it back up until your arms are straight. Use a spotter or safety bars when going heavy.",
    ),
    "Incline Bench" to listOf(
        "Set the bench to about 30 to 45 degrees and lie back with your eyes under the bar.",
        "Grip the bar a little wider than shoulder-width and lift it off the rack with your shoulder blades pulled back.",
        "Lower the bar to your upper chest, then press it back up over your shoulders.",
        "Keep your feet planted and your hips on the bench.",
    ),
    "Pec Dec" to listOf(
        "Sit in the machine with your back against the pad and the handles at chest height.",
        "With a slight bend in your elbows, bring the handles together in front of your chest.",
        "Squeeze your chest, then let the handles open slowly until you feel a stretch.",
        "Keep your shoulders down and back throughout.",
    ),
    "Chest Fly" to listOf(
        "Lie on a flat bench holding dumbbells above your chest, palms facing each other and elbows slightly bent.",
        "Open your arms wide in an arc, lowering the weights until you feel a stretch in your chest.",
        "Bring them back together above your chest, keeping the same bend in your elbows.",
        "Use a weight you can control at the bottom.",
    ),
    "Shrugs" to listOf(
        "Stand holding dumbbells at your sides or a barbell in front of your thighs.",
        "Lift your shoulders straight up towards your ears as high as you can.",
        "Pause, then lower slowly.",
        "Keep your arms straight and don't roll your shoulders.",
    ),
    "Lat Pull-down" to listOf(
        "Sit at the machine with your thighs under the pads and grip the bar a little wider than shoulder-width.",
        "Lean back slightly and pull the bar down to your upper chest, driving your elbows down and back.",
        "Squeeze your back, then let the bar rise slowly until your arms are straight.",
        "Don't swing your body to move the weight.",
    ),
    "Pull-ups" to listOf(
        "Hang from a bar with an overhand grip a little wider than your shoulders.",
        "Pull your shoulder blades down, then pull yourself up until your chin is over the bar.",
        "Lower yourself slowly until your arms are straight.",
        "Don't kick or swing. Use an assisted machine or a band until you can do full reps.",
    ),
    "Lat Prayers" to listOf(
        "Kneel facing a high cable, holding a rope or bar with your arms stretched overhead.",
        "Keeping your arms nearly straight, sweep the handle down in an arc as you bow your upper body slightly forward.",
        "Finish with your hands near your head and squeeze your lats.",
        "Return slowly to the stretched position.",
    ),
    "Bent-over Rows" to listOf(
        "Hold a barbell with an overhand grip and hinge at your hips until your torso is nearly level with the floor, back flat.",
        "Let the bar hang at arm's length below your shoulders.",
        "Pull it towards your lower ribs, driving your elbows back and squeezing your shoulder blades together.",
        "Lower it slowly, keeping your back flat and your torso still.",
    ),
    "Chest-Supported Rows" to listOf(
        "Lie face down on an incline bench or row machine with your chest on the pad, holding dumbbells or handles.",
        "Pull the weights up and back towards your hips, squeezing your shoulder blades together.",
        "Lower slowly until your arms are straight.",
        "Keep your chest on the pad so your lower back stays out of it.",
    ),
    "Leg Extensions" to listOf(
        "Sit in the machine with your back against the pad and the roller just above your ankles.",
        "Straighten your legs fully, squeezing your quads.",
        "Lower slowly to the start.",
        "Hold the handles to keep your hips on the seat.",
    ),
    "Behind-the-Back Bicep Curls" to listOf(
        "Stand facing away from a low cable, holding the handle in one hand with your arm slightly behind your body.",
        "Keeping your elbow back, curl the handle forward and up towards your shoulder.",
        "Lower slowly until your arm is straight and you feel a stretch in your biceps.",
        "Keep your upper arm still, then repeat on the other side.",
    ),
    "Smith-Squats" to listOf(
        "Stand under the Smith machine bar with it across your upper back, feet shoulder-width apart and slightly in front of you.",
        "Unrack the bar and squat down by bending your hips and knees until your thighs are at least level with the floor.",
        "Push through your feet to stand back up.",
        "Keep your back straight and your knees in line with your toes.",
    ),
    "Cable Lateral Raises" to listOf(
        "Stand side-on to a low cable and hold the handle with the hand furthest from the machine.",
        "With a slight bend in your elbow, raise your arm out to the side up to shoulder height.",
        "Pause, then lower slowly against the pull of the cable.",
        "Keep your torso still and don't shrug, then repeat on the other side.",
    ),
    "Upright Rows" to listOf(
        "Stand holding a barbell, dumbbells or cable bar in front of your thighs, hands about shoulder-width apart.",
        "Pull the weight straight up along your body, leading with your elbows, until it reaches about chest height.",
        "Lower it slowly.",
        "Stop lower if you feel pinching in your shoulders.",
    ),
    "Dumbbell Bench Press" to listOf(
        "Lie on a flat bench with a dumbbell in each hand at the sides of your chest, feet flat on the floor.",
        "Press the dumbbells up until your arms are straight above your chest.",
        "Lower them slowly to chest level, elbows at about 45 degrees to your body.",
        "Keep your shoulder blades pulled back against the bench.",
    ),
    "Incline Dumbbell Press" to listOf(
        "Set the bench to about 30 to 45 degrees and sit back with a dumbbell in each hand at your shoulders.",
        "Press the dumbbells up over your upper chest until your arms are straight.",
        "Lower them slowly back to your shoulders.",
        "Keep your back against the bench and your feet planted.",
    ),
    "Decline Bench Press" to listOf(
        "Lie on a decline bench with your legs secured under the pads.",
        "Lift the bar off the rack with a grip a little wider than shoulder-width.",
        "Lower it to your lower chest, then press it back up until your arms are straight.",
        "Use a spotter, as the bar is harder to rack from this position.",
    ),
    "Cable Crossovers" to listOf(
        "Set both pulleys high and stand in the middle with a handle in each hand, one foot slightly forward.",
        "With a slight bend in your elbows, bring your hands down and together in front of you.",
        "Squeeze your chest, then let your arms open slowly until you feel a stretch.",
        "Keep your torso still and the bend in your elbows the same throughout.",
    ),
    "Push-ups" to listOf(
        "Place your hands on the floor slightly wider than your shoulders, body straight from head to heels.",
        "Lower your chest towards the floor, elbows at about 45 degrees to your body.",
        "Push back up until your arms are straight.",
        "Keep your core tight so your hips don't sag. Rest on your knees to make them easier.",
    ),
    "Machine Chest Press" to listOf(
        "Adjust the seat so the handles line up with your mid chest, then sit with your back against the pad.",
        "Press the handles forward until your arms are straight, without locking your elbows hard.",
        "Return slowly until you feel a stretch in your chest.",
        "Keep your shoulder blades back against the pad.",
    ),
    "Chin-ups" to listOf(
        "Hang from a bar with an underhand grip, hands about shoulder-width apart.",
        "Pull yourself up until your chin is over the bar, driving your elbows down.",
        "Lower yourself slowly until your arms are straight.",
        "Don't swing. Use an assisted machine or a band if you need to.",
    ),
    "Straight-Arm Pulldowns" to listOf(
        "Stand facing a high cable with a bar or rope, arms straight out in front of you at about head height.",
        "Keeping your arms straight, pull the handle down in an arc to your thighs.",
        "Squeeze your lats, then let it rise slowly back to the start.",
        "Lean slightly forward from your hips and keep your torso still.",
    ),
    "Seated Cable Rows" to listOf(
        "Sit at the row machine with your feet on the platform, knees slightly bent, holding the handle with straight arms.",
        "Sit tall and pull the handle to your stomach, driving your elbows back and squeezing your shoulder blades together.",
        "Let it return slowly until your arms are straight.",
        "Don't rock your torso back and forth to move the weight.",
    ),
    "Single-Arm Dumbbell Rows" to listOf(
        "Place one knee and the same hand on a bench, back flat, with a dumbbell in your other hand.",
        "Pull the dumbbell up towards your hip, keeping your elbow close to your body.",
        "Lower it slowly until your arm is straight.",
        "Keep your back flat and don't twist, then repeat on the other side.",
    ),
    "T-Bar Rows" to listOf(
        "Stand over the bar with a foot on either side, hinge at your hips with a flat back and hold the handles.",
        "Pull the bar up towards your chest, driving your elbows back.",
        "Lower it slowly until your arms are straight.",
        "Keep your back flat and your knees slightly bent.",
    ),
    "Deadlift" to listOf(
        "Stand with your feet hip-width apart and the bar over the middle of your feet.",
        "Hinge down and grip the bar just outside your legs, then bend your knees until your shins touch it. Keep your back flat and chest up.",
        "Brace your core and stand up by pushing the floor away, keeping the bar close to your legs.",
        "Finish standing tall with your hips and knees straight, then lower the bar the same way.",
    ),
    "Overhead Press" to listOf(
        "Stand holding a barbell at your upper chest, hands just wider than your shoulders.",
        "Brace your core and squeeze your glutes, then press the bar straight up, moving your head back slightly to let it pass.",
        "Finish with the bar over the middle of your feet, then lower it to your chest with control.",
        "Don't lean back to push the weight up.",
    ),
    "Arnold Press" to listOf(
        "Sit holding dumbbells in front of your shoulders, palms facing you.",
        "Press the weights up, turning your palms to face forward as you go.",
        "Finish with your arms straight overhead.",
        "Lower them back down, turning your palms to face you again.",
    ),
    "Front Raises" to listOf(
        "Stand holding dumbbells or a plate in front of your thighs.",
        "With a slight bend in your elbows, raise the weight in front of you up to shoulder height.",
        "Pause, then lower slowly.",
        "Keep your torso still and don't swing the weight.",
    ),
    "Rear Delt Flyes" to listOf(
        "Hinge forward at your hips with a flat back, holding dumbbells below your chest, palms facing each other.",
        "With a slight bend in your elbows, raise your arms out to the sides until they're in line with your body.",
        "Squeeze the backs of your shoulders, then lower slowly.",
        "You can also do these face down on an incline bench, or on a reverse pec dec.",
    ),
    "Hammer Curls" to listOf(
        "Stand with a dumbbell in each hand at your sides, palms facing your body.",
        "Keeping your elbows at your sides, curl the weights up towards your shoulders without turning your wrists.",
        "Lower slowly until your arms are straight.",
        "Don't swing your body.",
    ),
    "Cable Curls" to listOf(
        "Stand facing a low cable with a straight bar or rope, arms straight in front of your thighs.",
        "Keep your elbows at your sides and curl the handle up towards your shoulders.",
        "Squeeze your biceps, then lower slowly until your arms are straight.",
        "Stand tall and don't lean back.",
    ),
    "Concentration Curls" to listOf(
        "Sit on a bench with your feet wide, holding a dumbbell in one hand.",
        "Rest the back of that upper arm against your inner thigh, arm straight.",
        "Curl the dumbbell up towards your shoulder, then lower it slowly.",
        "Keep your upper arm still against your leg, then repeat on the other side.",
    ),
    "Dips" to listOf(
        "Support yourself on parallel bars with straight arms and your shoulders down.",
        "Lower yourself by bending your elbows until your upper arms are about level with the floor.",
        "Push back up until your arms are straight.",
        "Lean forward slightly for more chest, stay upright for more triceps. Use an assisted machine if you need to.",
    ),
    "Close-Grip Bench Press" to listOf(
        "Lie on a flat bench and grip the bar about shoulder-width apart.",
        "Lower the bar to your lower chest, keeping your elbows close to your sides.",
        "Press it back up until your arms are straight.",
        "Don't grip too narrow, as that strains your wrists.",
    ),
    "Tricep Kickbacks" to listOf(
        "Hinge forward with a flat back, holding a dumbbell with your upper arm tucked against your side and elbow bent at 90 degrees.",
        "Keeping your upper arm still, straighten your elbow until your arm points behind you.",
        "Squeeze your triceps, then return slowly.",
        "Support yourself on a bench with your other hand if you need to.",
    ),
    "Front Squats" to listOf(
        "Rest the bar on the front of your shoulders with your elbows high, holding it with your fingertips or arms crossed.",
        "With your feet about shoulder-width apart, squat down keeping your torso upright and elbows up.",
        "Go as deep as you can while keeping your back straight.",
        "Drive through your whole foot to stand back up.",
    ),
    "Bulgarian Split Squats" to listOf(
        "Stand about a stride in front of a bench and rest the top of your back foot on it.",
        "Holding dumbbells at your sides if you like, lower your back knee towards the floor.",
        "Go down until your front thigh is about level with the floor, then push through your front foot to stand up.",
        "Keep your torso upright and your front knee in line with your toes, then switch legs.",
    ),
    "Goblet Squats" to listOf(
        "Hold a dumbbell or kettlebell against your chest with both hands, feet about shoulder-width apart.",
        "Squat down between your knees, keeping your chest up and your elbows inside your knees.",
        "Go as deep as you comfortably can with your heels down.",
        "Push through your feet to stand back up.",
    ),
    "Wall Sit" to listOf(
        "Stand with your back flat against a wall, feet about shoulder-width apart and a little in front of you.",
        "Slide down until your knees are bent to about 90 degrees, thighs level with the floor.",
        "Hold the position with your back against the wall and your weight in your heels.",
        "Keep breathing and don't rest your hands on your legs.",
    ),
    "Romanian Deadlift" to listOf(
        "Stand holding a barbell or dumbbells in front of your thighs, feet hip-width apart and knees slightly bent.",
        "Push your hips back and lower the weight along your legs, keeping your back flat.",
        "Go down until you feel a strong stretch in your hamstrings, usually around mid shin.",
        "Drive your hips forward to stand back up, squeezing your glutes.",
    ),
    "Seated Leg Curls" to listOf(
        "Sit in the machine with the thigh pad holding your legs down and the roller behind your lower calves.",
        "Curl your legs down and back as far as you can.",
        "Pause, then let them return slowly until your legs are straight.",
        "Keep your back against the pad.",
    ),
    "Good Mornings" to listOf(
        "Rest a light barbell across your upper back, feet hip-width apart and knees slightly bent.",
        "Push your hips back and lean your torso forward with a flat back until you feel a stretch in your hamstrings.",
        "Drive your hips forward to stand back up.",
        "Start light, as control matters more than weight here.",
    ),
    "Glute Bridges" to listOf(
        "Lie on your back with knees bent and feet flat on the floor, hip-width apart.",
        "Push through your heels and lift your hips until your body is straight from shoulders to knees.",
        "Squeeze your glutes at the top, then lower slowly.",
        "Rest a weight across your hips to make it harder.",
    ),
    "Cable Kickbacks" to listOf(
        "Attach an ankle strap to a low cable and face the machine, holding it for balance.",
        "With a slight bend in your knee, kick the strapped leg back and up behind you.",
        "Squeeze your glute at the top, then return slowly.",
        "Keep your back flat and don't swing, then repeat on the other leg.",
    ),
    "Hip Abductions" to listOf(
        "Sit in the machine with your back against the pad and the pads on the outside of your knees.",
        "Push your knees outwards as far as you can.",
        "Pause, then let them come back together slowly.",
        "Leaning slightly forward works your glutes more.",
    ),
    "Seated Calf Raises" to listOf(
        "Sit in the machine with the balls of your feet on the platform and the pad resting on your lower thighs.",
        "Lower your heels as far as you can to stretch your calves.",
        "Push up onto the balls of your feet as high as you can and pause.",
        "Lower slowly and repeat.",
    ),
    "Plank" to listOf(
        "Rest on your forearms and toes with your elbows under your shoulders.",
        "Keep your body in a straight line from head to heels.",
        "Brace your core and squeeze your glutes so your hips don't sag or rise.",
        "Hold the position and keep breathing.",
    ),
    "Side Plank" to listOf(
        "Lie on your side and prop yourself up on one forearm, elbow under your shoulder and legs stacked.",
        "Lift your hips so your body is straight from head to feet.",
        "Hold the position without letting your hips drop.",
        "Repeat on the other side.",
    ),
    "Crunches" to listOf(
        "Lie on your back with knees bent and feet flat, hands on your chest or lightly by your ears.",
        "Curl your shoulders off the floor by tightening your abs.",
        "Pause at the top, then lower slowly.",
        "Keep your lower back on the floor and don't pull on your neck.",
    ),
    "Cable Crunches" to listOf(
        "Kneel facing a high cable with a rope, holding it beside your head.",
        "Crunch down by curling your spine, bringing your elbows towards your thighs.",
        "Squeeze your abs at the bottom, then return slowly.",
        "Keep your hips still so your abs do the work, not your arms.",
    ),
    "Russian Twists" to listOf(
        "Sit on the floor with knees bent, lean back slightly and lift your feet if you can, holding a weight at your chest.",
        "Turn your torso to one side, bringing the weight beside your hip.",
        "Turn to the other side.",
        "Keep your back straight and move with control.",
    ),
    "Hanging Leg Raises" to listOf(
        "Hang from a pull-up bar with straight arms.",
        "Keeping your legs straight or bent, raise them in front of you to at least hip height.",
        "Lower them slowly without swinging.",
        "Curl your hips up at the top to work your abs harder.",
    ),
    "Ab Wheel Rollouts" to listOf(
        "Kneel holding the ab wheel on the floor in front of your knees.",
        "Brace your core and roll the wheel forward as far as you can without your lower back sagging.",
        "Use your abs to roll back to the start.",
        "Start with short rollouts and go further as you get stronger.",
    ),
)
