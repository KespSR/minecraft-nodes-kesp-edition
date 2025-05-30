/**
 * Serializer
 * 
 * Performs manual JSON string serialization of
 * player-editable game structures (resident, town, nation)
 * 
 * Manual JSON string serialization is for performance
 * of world state writes, to generate a minified JSON String.
 * Total save time is then blocked purely by File IO.
 * 
 */

package phonon.nodes.serdes

import phonon.nodes.Nodes
import phonon.nodes.objects.Resident.ResidentSaveState
import phonon.nodes.objects.Town.TownSaveState
import phonon.nodes.objects.Nation.NationSaveState


public object Serializer {
    
    fun worldToJson(
        residents: List<ResidentSaveState>,
        towns: List<TownSaveState>,
        nations: List<NationSaveState>
    ): String {

        // calculate string builder capacity
        
        // initial metadata header + close bracket [26]: {"meta":{"type":"towns"},}
        // residents header + close bracket + comma [15]: "residents":{},
        // towns header + close bracket + comma [11]: "towns":{},
        // nations header + close bracket [13]: "nations":{}}
        // -> 65 minimum
        // will add arbitrary extra margin and up size to 200
        var bufferSize = 200
        
        // residents:
        // format: "uuid":{...},
        // uuid - 36 chars
        // 2 '"', ":", and "," - 4 chars
        for ( v in residents ) {
            bufferSize += (40 + v.toJsonString().length)
        }

        // towns:
        // format: "name":{...},
        for ( v in towns ) {
            bufferSize += (4 + v.name.length + v.toJsonString().length)
        }

        // nations:
        // format: "name":{...},
        for ( v in nations ) {
            bufferSize += (4 + v.name.length + v.toJsonString().length)
        }

        // json string builder
        val jsonString = StringBuilder(bufferSize)


        // ===============================
        // Metadata (for web editor)
        // ===============================
        jsonString.append("{\"meta\":{\"type\":\"towns\"},")

        // ===============================
        // Residents
        // ===============================
        jsonString.append("\"residents\":{")

        for ( (i, resident) in residents.withIndex() ) {
            jsonString.append("\"${resident.uuid.toString()}\":")
            jsonString.append(resident.toJsonString())
            if ( i < residents.size - 1 ) {
                jsonString.append(",")
            }
        }

        jsonString.append("},")

        // ===============================
        // Towns
        // ===============================
        jsonString.append("\"towns\":{")

        for ( (i, town) in towns.withIndex() ) {
            jsonString.append("\"${town.name}\":")
            jsonString.append(town.toJsonString())
            if ( i < towns.size - 1 ) {
                jsonString.append(",")
            }
        }

        jsonString.append("},")

        // ===============================
        // Nations
        // ===============================
        jsonString.append("\"nations\":{")

        for ( (i, nation) in nations.withIndex() ) {
            jsonString.append("\"${nation.name}\":")
            jsonString.append(nation.toJsonString())
            if ( i < nations.size - 1 ) {
                jsonString.append(",")
            }
        }

        jsonString.append("}}")

        // ===============================

        return jsonString.toString()
    }
}
