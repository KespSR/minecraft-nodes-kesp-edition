/**
 * Gestor de solicitudes para crear puertos.
 *
 *  • guarda / carga en “plugins/ports/requests.json”
 *  • permite a los administradores ver la lista y aceptar/denegar con ClickEvent
 */

package phonon.ports

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.collections.ArrayList

/* ========================================================================== */
/*  Modelo                                                                    */
/* ========================================================================== */

data class PortRequest(
    val id: UUID = UUID.randomUUID(),
    val player: String,        // nick del jugador que hace la solicitud
    val portName: String,      // ← NUEVO  nombre que pide el jugador
    val x: Int,
    val z: Int,
    val groups: List<String>   // ← NUEVO  uno o más grupos
)


/* ========================================================================== */
/*  Servicio singleton                                                        */
/* ========================================================================== */

object PortRequests {

    /** Path donde se guardan las peticiones (plugins/ports/requests.json) */
    private val path: Path = Ports.dirSave.resolve("requests.json")

    private val gson = Gson()

    /** Lista en memoria, protegida con sincronización simple */
    private val requests: MutableList<PortRequest> =
        Collections.synchronizedList(ArrayList())

    /* ---------------------------------------------------------------------- */
    /*  API pública                                                            */
    /* ---------------------------------------------------------------------- */

    /** Carga las solicitudes desde disco (si el fichero existe) */
    fun load() {
        requests.clear()
        if (!Files.exists(path)) return

        val type = TypeToken.getParameterized(List::class.java, PortRequest::class.java).type
        val list: List<PortRequest> = gson.fromJson(Files.readString(path), type) ?: return
        requests.addAll(list)
    }

    /** Guarda la lista actual en disco */
    fun save() {
        Files.createDirectories(path.parent)
        Files.writeString(
            path,
            gson.toJson(requests),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    }

    fun add(req: PortRequest) {
        requests += req
        save()
    }

    fun remove(id: UUID) {
        requests.removeIf { it.id == id }
        save()
    }

    /** Lista inmutable para lectura */
    fun all(): List<PortRequest> = requests.toList()

    /* ---------------------------------------------------------------------- */
    /*  Mensaje formateado para los admins                                     */
    /* ---------------------------------------------------------------------- */

    /**
     * Envía al administrador la lista de solicitudes con botones [TP] [ACCEPT] [DENY].
     */
    fun sendListToAdmin(admin: CommandSender) {
        if (requests.isEmpty()) {
            Message.print(admin, "${ChatColor.GRAY}No hay solicitudes pendientes")
            return
        }

        requests.forEachIndexed { idx, r ->
            val base = TextComponent(
                "${ChatColor.DARK_AQUA}#${idx + 1} " +
                        "${ChatColor.AQUA}${r.player} " +
                        "${ChatColor.WHITE}(${r.x}, ${r.z}) " +
                        "${ChatColor.DARK_GRAY}[${r.groups.joinToString(",")}] "
            )

            val tp = TextComponent("[TP]").apply {
                color = net.md_5.bungee.api.ChatColor.GOLD
                clickEvent = ClickEvent(RUN_COMMAND, "/tp ${r.x} ${Ports.seaLevel.toInt()} ${r.z}")
            }

            val accept = TextComponent(" [ACCEPT]").apply {
                color = net.md_5.bungee.api.ChatColor.GREEN
                clickEvent = ClickEvent(RUN_COMMAND, "/portadmin accept ${r.id}")
            }

            val deny = TextComponent(" [DENY]").apply {
                color = net.md_5.bungee.api.ChatColor.RED
                clickEvent = ClickEvent(RUN_COMMAND, "/portadmin deny ${r.id}")
            }

            base.addExtra(tp); base.addExtra(accept); base.addExtra(deny)

            when (admin) {
                is Player -> admin.spigot().sendMessage(base)
                else      -> admin.sendMessage(base.toPlainText())
            }
        }
    }
}
