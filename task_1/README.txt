Rumei Ma 2690683
Dennis Grotz 2360065
Vincenz Mechler 2817217

Rollen der Gruppenmitglied:
	Vincenz:
		-erstellen der Grundstruktur 
		-Timing Cost 
		-place Funktion
		-SimplePath
		-gelegentlich dokumentation
		
	Rumei und Dennis:
		-WiringCost
		-main- und IO-Funktionen
		-Dokumentation vervollständigt
		-Testen

Die Leistungsdaten des verwendeten Testrechners: Prozessor: Intel Core i3-8130U Takt: 3.1GHz Speicher: DDR3 4GB Betriebssystem: Windows 10 64-bit 

Ausführen des Programms:
	java -jar placer.jar [absoluter pfad netlist file] [absoluter pfad arch file] [absoluter pfad destination von .p file] -X 8 -Y 8
	Flags:
		-diagnoseData: print and plot data
		-lambda [value(float)]: custom wert für lambda, default 0,5
		-stepCountFactor [value(int)]: custom wert für Schrittzahl Multiplikator, default 10
		
Commithistory aus git:

commit e3fa84e530e4136607e4738045e745cfc98203d8 (HEAD -> task_2)
Merge: 9d0b6e6 bcb43d3
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 21:28:21 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 9d0b6e633aaa2b0f7cb1989d67ee0ca8faedc792
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 21:27:34 2019 +0100

    added documentation

commit bcb43d37030acfa685b06577166c353b2c23e2bd (origin/task_2)
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 21:07:30 2019 +0100

    added raw diagnostic data printing method (output to files)

commit 1ec74a756cac3ab6a188ce99b8833c085f317eb6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 20:53:44 2019 +0100

:...skipping...
commit e3fa84e530e4136607e4738045e745cfc98203d8 (HEAD -> task_2)
Merge: 9d0b6e6 bcb43d3
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 21:28:21 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 9d0b6e633aaa2b0f7cb1989d67ee0ca8faedc792
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 21:27:34 2019 +0100

    added documentation

commit bcb43d37030acfa685b06577166c353b2c23e2bd (origin/task_2)
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 21:07:30 2019 +0100

    added raw diagnostic data printing method (output to files)

commit 1ec74a756cac3ab6a188ce99b8833c085f317eb6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 20:53:44 2019 +0100

    final phase

commit 6fd25311e5dd2b35fee42ee61d2d495b27b60693
Merge: ab45248 90d2f55
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 20:12:10 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit ab4524831de7806a6973c8a666e47f7396ad1df2
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 20:12:04 2019 +0100

    nothing changed much

commit 90d2f558e3e9fb8b2df72604585ebb82d8145ff5
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 20:05:44 2019 +0100

    fixed initial temperature computation and outer loop creterion

commit f4535fbca2b64c07fdb78fc7d5f5c812ef2c33a2
Merge: 9efe849 434bc91
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 19:37:03 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 9efe8498efd9f7258ecaa3a2888989c2af4cd3cd
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 19:36:57 2019 +0100

    output

commit 434bc9154a72cbe2342afcdcdf823dfe9303e22d
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 19:35:58 2019 +0100

    fixed rLimit update bug

    fixed wiringCostDelta NaN bug

commit e71d6a4560b2936cf561f1c9a2a517d7ab023986
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 17:35:01 2019 +0100

    added test files in bibliothek

commit c91f2ba677ab270164d480fb9712a595ac5b3806
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 17:30:47 2019 +0100

    fixed bug in random io block placement

commit 286033a77573343d356e1f05178eef56750dc6f6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 17:29:04 2019 +0100

    added library file

commit e4a48247100475ccae3e601d43eeba48dacc11a2
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 17:25:27 2019 +0100

    still bugged

commit 82bf909397dc434906e2f5d1dee0ac162216a584 (refs/stash)
Merge: cdd4d62 4d5b249
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 16:02:22 2019 +0100

    WIP on (no branch): cdd4d62 fixed remaining obvious buggs

commit 4d5b2494114d736ddd6339b900af343f38ba573f
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 16:02:21 2019 +0100

    index on (no branch): cdd4d62 fixed remaining obvious buggs

commit 03caa4474b51df4e614b9105e076a156879ccef6
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 15:57:27 2019 +0100

    need to reverse, NAN

commit da21b1aa360397cf20a952eb9bdb56a099fb27d7
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 15:28:21 2019 +0100

    series for plotting added

commit 39fcea6978d7177eaf2222ad78d300636e872c99
Merge: 2ff19d3 4c22757
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 14:33:40 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 2ff19d3b26fb00871f2bbfee4b22f1a185dd9229
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 14:33:19 2019 +0100

    added diagnoseData

commit 4c22757e9a9010af49edabdc52045441716613ca
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 14:32:37 2019 +0100

    fixed initial temperature computation and introduced global timing and total cost buffers

commit cdd4d62ad339596f6f3da57fd70dd80120a52910
Author: Rumei <rumei.ma@gmail.com>
Date:   Wed Jan 16 13:06:06 2019 +0100

    fixed remaining obvious buggs

commit 122ba51ff4114b773ef06c60f49ca1693c63c20a
Author: Vincenz Mechler <011000101101@gmx.de>
Date:   Wed Jan 16 11:44:33 2019 +0100

    fixed ioBlockSwap bug and added support for double command line arguments, started reworking the initial temperature function

commit 8e99b09a3a38bedafecceaa08667fa3207b300c2
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 21:31:20 2019 +0100

    die hoffnung stirbt zu letzt

commit cdddd0ff0d39273112b54a12b14867fe4938559e
Merge: 8cac5d4 135ad63
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 20:56:55 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 8cac5d4358c6ea5b10affcfa4030e9aa540fffb6
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 20:56:47 2019 +0100

    debuggen -rumei

commit 135ad637389d02c17c8f2df8058052078d07d146
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 20:56:15 2019 +0100

    fixed timing cost bug

commit 1dc27fa487f95154ab76b9f3702aca0915925223
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 20:30:31 2019 +0100

    fixed various bugs

commit aad8adc8c6a65f9a50276b4f498f8e5a31229914
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 19:26:28 2019 +0100

    created cache for wiring cost

commit edc2d95f79b6f69035fa18fd3137b98367574a5f
Merge: 72f76fe e2bcd7a
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 19:22:03 2019 +0100

    merge
    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit e2bcd7a07dcf8c777bfce34e546df0835838bdea
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 19:21:53 2019 +0100

    moved wiring cost to Net(class)

commit 72f76feb6eeda396f41a74c33d77a184f7eebc22
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 19:20:48 2019 +0100

    fixed initial block placement method and applySwap method

commit 510eedc4da5ad7bd9bdc2d86ed7132c9c93b12aa
Merge: 7e7c59f 076304c
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 18:14:59 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 7e7c59f31c15d290b4be6207d146d6dfc01d73a6
Author: Rumei <rumei.ma@gmail.com>
Date:   Tue Jan 15 18:12:00 2019 +0100

    nothing changed?

commit 076304c241d9709653f4d9d8d751c73ff2b22793
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Tue Jan 15 00:32:05 2019 +0100

    finished first implementation and started debugging

commit bd50649a9e40b7cf218324405a49770329c44124
Merge: 8cf4bd7 0a8a62c
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 16:42:36 2019 +0100

    merge
    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 0a8a62c82643a0bbbe9b025195d5e87f30a5332b
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 16:38:47 2019 +0100

    parseCommandlineArgument updated

commit 8cf4bd7e2057382ada9af7bc49e83297ca790302 (origin/task_2_vincenz)
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 16:38:18 2019 +0100

    implemented some TODOs and removed unnecessary methods

commit b21f958b04893d235966e049c78f77c3b3b37eec
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 15:57:11 2019 +0100

    getBlocks() in class Net and getNet() in class NetlistBlock implemented

commit 9c370e1ebef98dd43df92513d40b00c8d73d69ec
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 15:30:31 2019 +0100

    removed fullfilled TODOs and fixed bug in simplePath generation in Placer.place()

commit 0603da4645eccdc64aee3dcef9858d393a22a8ca
Merge: f0d773d 5283291
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 15:25:52 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit f0d773d29c77859cc82b6132cdb8055695b2b8e9
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 15:16:36 2019 +0100

    commit before pull required

commit 5283291ff0e38da9199142a2be3a6e3e37e6512e
Merge: c62f6dc 0c7526c
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 15:04:56 2019 +0100

    merged

commit c62f6dc54081f15c9c46eb55c1a1ef89f6deca73
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 15:02:21 2019 +0100

    cleaned up TODOs

commit 0c7526c335a16438453ba7a0d52ef8fa3c471daa
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 14:47:09 2019 +0100

    wiring cost and delta wiring cost kinda finished, needs to be checked by
    Lord Vincenz

commit db355a3b63e120b444ddd652e4dc2ecc740dd52d
Merge: 99a1523 a15b6a0
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 13:43:50 2019 +0100

    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit 99a1523b58256dce7b7d14d1c3fe780d7f9b8aab
Author: Rumei <rumei.ma@gmail.com>
Date:   Mon Jan 14 13:43:37 2019 +0100

    few minor changes

commit a15b6a03c1cbdff61c6c88aa739e3fb851cc6648
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 13:35:44 2019 +0100

    implemented PlacementWriter and AbstractWriter and staged new files for commit...

commit 2dc44e6f3d9598e2d9d323dc72051401e8d9b810
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 13:34:35 2019 +0100

    implemented PlacementWriter and AbstractWriter

commit 570ed2640acbca5aa26ed0aad4aa5328483fb2e0
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 14 01:52:19 2019 +0100

    finished implementation of timing cost

commit d2c66f0a6f09967af62695429ea24783a0652a83
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Sun Jan 13 22:10:57 2019 +0100

    implemented timing analysis (ta and slack annotation)

commit 2315faa04cb528614dffeff654931b3d3f88f173
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Sun Jan 13 18:00:59 2019 +0100

    merged and fixed compilation errors

commit 4b83a0b5725d332a7aada31cd8b9f9eb4c69c715
Merge: aa2ff5d bc413ab
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Sun Jan 13 17:57:30 2019 +0100

    merge
    Merge branch 'task_2' of https://github.com/011000101101/ACE into task_2

commit aa2ff5d8622d6b77c8dd9da17606572b95526464
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Sun Jan 13 17:57:24 2019 +0100

    completed swapping methods

commit bc413ab93af60b9ae638efea7e62e2c1d0e7f249
Author: Rumei <rumei.ma@gmail.com>
Date:   Sun Jan 13 16:16:58 2019 +0100

    WiringCost started to implement

commit b15732a6eac07f2d15b619b7a29cb438927137b6
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Sun Jan 13 13:41:49 2019 +0100

    changed placement structure and began with block swap implementation

commit 294e7f80014cd9074f954bc434bf221c0756dbde
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Mon Jan 7 23:11:39 2019 +0100

    implemented logic of inner loop and changed cost computation structure

commit 2331cf8aef0e0321fdf12999cbe052273dd44545
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Thu Jan 3 05:05:49 2019 +0100

    changed iOBlocks ańd logicBlocks from lists to Arays for efficient access

commit ca89f6eafbec5c5a41e4b3f5e843ac7100dbe847
Author: Vincenz Mechler <vincenz.mechler@gmx.de>
Date:   Thu Jan 3 03:35:27 2019 +0100

    begun implementation of Placer.java, implemented main method, algorithm control flow and initial placement generator methods
