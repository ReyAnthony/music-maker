import Notes.*
import Scale.PENTATONIC_MINOR
import java.lang.System.currentTimeMillis
import java.security.SecureRandom.getInstanceStrong
import java.util.*
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage
import javax.sound.midi.ShortMessage.NOTE_OFF
import javax.sound.midi.ShortMessage.NOTE_ON

val OCTAVE = 12

enum class Notes(val value: Int) {
    
    SILENCE(-1),

    C0(0), C_SHARP_0(1),
    D0(2), D_SHARP_0(3),
    E0(4),
    F0(5), F_SHARP_0(6),
    G0(7), G_SHARP_0(8),
    A0(9), A_SHARP_0(10),
    B0(11),

    C1(C0.value + OCTAVE), C_SHARP_1(C_SHARP_0.value + OCTAVE),
    D1(D0.value + OCTAVE), D_SHARP_1(D_SHARP_0.value + OCTAVE),
    E1(E0.value + OCTAVE),
    F1(F0.value + OCTAVE), F_SHARP_1(F_SHARP_0.value + OCTAVE),
    G1(G0.value + OCTAVE), G_SHARP_1(G_SHARP_0.value + OCTAVE),
    A1(A0.value + OCTAVE), A_SHARP_1(A_SHARP_0.value + OCTAVE),
    B1(B0.value + OCTAVE),

    C2(C1.value + OCTAVE), C_SHARP_2(C_SHARP_1.value + OCTAVE),
    D2(D1.value + OCTAVE), D_SHARP_2(D_SHARP_1.value + OCTAVE),
    E2(E1.value + OCTAVE),
    F2(F1.value + OCTAVE), F_SHARP_2(F_SHARP_1.value + OCTAVE),
    G2(G1.value + OCTAVE), G_SHARP_2(G_SHARP_1.value + OCTAVE),
    A2(A1.value + OCTAVE), A_SHARP_2(A_SHARP_1.value + OCTAVE),
    B2(B1.value + OCTAVE),

    C3(C2.value + OCTAVE), C_SHARP_3(C_SHARP_2.value + OCTAVE),
    D3(D2.value + OCTAVE), D_SHARP_3(D_SHARP_2.value + OCTAVE),
    E3(E2.value + OCTAVE),
    F3(F2.value + OCTAVE), F_SHARP_3(F_SHARP_2.value + OCTAVE),
    G3(G2.value + OCTAVE), G_SHARP_3(G_SHARP_2.value + OCTAVE),
    A3(A2.value + OCTAVE), A_SHARP_3(A_SHARP_2.value + OCTAVE),
    B3(B2.value + OCTAVE),

    C4(C3.value + OCTAVE), C_SHARP_4(C_SHARP_3.value + OCTAVE),
    D4(D3.value + OCTAVE), D_SHARP_4(D_SHARP_3.value + OCTAVE),
    E4(E3.value + OCTAVE),
    F4(F3.value + OCTAVE), F_SHARP_4(F_SHARP_3.value + OCTAVE),
    G4(G3.value + OCTAVE), G_SHARP_4(G_SHARP_3.value + OCTAVE),
    A4(A3.value + OCTAVE), A_SHARP_4(A_SHARP_3.value + OCTAVE),
    B4(B3.value + OCTAVE),

    C5(C4.value + OCTAVE), C_SHARP_5(C_SHARP_4.value + OCTAVE),
    D5(D4.value + OCTAVE), D_SHARP_5(D_SHARP_4.value + OCTAVE),
    E5(E4.value + OCTAVE),
    F5(F4.value + OCTAVE), F_SHARP_5(F_SHARP_4.value + OCTAVE),
    G5(G4.value + OCTAVE), G_SHARP_5(G_SHARP_4.value + OCTAVE),
    A5(A4.value + OCTAVE), A_SHARP_5(A_SHARP_4.value + OCTAVE),
    B5(B4.value + OCTAVE);

}

fun fromInt(midiNote: Int) : String {

    for (n in Notes.values()){
        if(midiNote == n.value)
            return n.name
    }

    return "Unknown note"

}

enum class Scale (){

    PENTATONIC_MINOR,
    PENTATONIC_MAJOR,
    MAJOR,
    MINOR

}

fun getNoteDurations(durationOf1Time: Int): IntArray {

    fun white  () : Int { return durationOf1Time * 2 }
    fun round  () : Int { return durationOf1Time * 4 }
    fun black  () : Int { return durationOf1Time }
    fun half   () : Int { return durationOf1Time / 2}
    fun quarter() : Int { return durationOf1Time / 4}

    return intArrayOf(
                    black(), black(), black(),
                    half(),
                    quarter(),
                    white(), white(),
                    round(), round())
}

fun getScale(baseNote: Notes, scale: Scale) : IntArray {

    var generatedScale = intArrayOf(SILENCE.value)

    if(scale == PENTATONIC_MINOR){
        generatedScale =
                intArrayOf(
                        baseNote.value,
                        baseNote.value.plus(3),
                        baseNote.value.plus(5),
                        baseNote.value.plus(7),
                        baseNote.value.plus(10)
                )
    }

    return generatedScale
}

fun main(args : Array<String>){

    val durationOf1Time = (Random().nextInt(700) + 800).coerceAtMost(1500)
    println(durationOf1Time)

    val scale =
            getScale(G2, PENTATONIC_MINOR)
                    .plus(getScale(G3, PENTATONIC_MINOR))
                    .plus(SILENCE.value)
                    .plus(SILENCE.value)
                    .plus(SILENCE.value)

    val notesDurations = getNoteDurations(durationOf1Time)

    val synth = MidiSystem.getSynthesizer()
    synth.open()
    synth.loadAllInstruments(synth.defaultSoundbank)

    val instrumentChange = ShortMessage()
    instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, 10, 114, 0)
    synth.receiver.send(instrumentChange, -1)

    instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 35, 0)
    synth.receiver.send(instrumentChange, -1)

    while(true) {

        val note = scale[getInstanceStrong().nextInt(scale.size)]
        println(fromInt(note))

        if(note != SILENCE.value){

            //basse
            var msg = ShortMessage()
            msg.setMessage(NOTE_ON, 0, note , 80)
            synth.receiver.send(msg, -1)

            //percu
            msg = ShortMessage()
            msg.setMessage(NOTE_ON, 10, note , 80)
            synth.receiver.send(msg, -1)
        }

        val tStart = currentTimeMillis()

        while(!synth.voiceStatus.filter { vs -> vs.active == true }.isEmpty()) {

            val tEnd = System.currentTimeMillis()
            val tDelta = tEnd - tStart

            if(tDelta > notesDurations[getInstanceStrong().nextInt(notesDurations.size)]){

                if(note != SILENCE.value) {

                    var msg = ShortMessage()
                    msg.setMessage(NOTE_OFF, 0, note, 80)
                    synth.receiver.send(msg, -1)

                    msg = ShortMessage()
                    msg.setMessage(NOTE_OFF, 10, note, 80)
                    synth.receiver.send(msg, -1)
                }
                break
            }
        }
    }

}
