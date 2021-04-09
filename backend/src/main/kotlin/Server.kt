import com.surrus.common.di.initKoin
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.surrus.common.remote.PeopleInSpaceApi
import com.surrus.common.remote.AstroResult
import com.surrus.common.remote.Assignment
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.content.resources
import io.ktor.http.content.static
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class ProtoCity(val name;) {
}

interface ProtobufSerialable<T, P> {
    fun serialize(payload : T) : P;
    fun deserialize(buffer : P) : T;
}

object Cities: IntIdTable() {
    var name = varchar("name", 50)
}

class City(id: EntityID<Int>) : IntEntity(id), ProtobufSerialable<City, ProtoCity> {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name

    override fun serialize(payload: City): ProtoCity {
        val proto = ProtoCity {
            name = payload.name
        }
        return proto;
    }

    override fun deserialize(buffer: ProtoCity): City {
        return City.new {
            name = buffer.name
        }; // FIXME
    }
}


fun main() {
    val koin = initKoin(enableNetworkLogs = true).koin
    val peopleInSpaceApi = koin.get<PeopleInSpaceApi>()
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        // psuedo

        // val table = initTable(ProtobufModel)
        // ^ creates a mapping between protobuf properties and db fields

        SchemaUtils.create (Cities)

        val products = IntIdTable("product")
        products.registerColumn<String>("name", VarCharColumnType());
        products.registerColumn<Float>("price", DecimalColumnType(10, 2));
        SchemaUtils.create (products)

//        val entityClass = EntityClass(products, ProductEntity)



        products.insert {
            it[name] = "Toys"
            it[price] = 2.50
        }

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        val stPete = City.new {
            name = "St. Petersburg"
        }

        // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
        println("Cities: ${City.all()}")
    }

    embeddedServer(Netty, 9090) {
        install(ContentNegotiation) {
            json()
        }

        routing {

            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }

            static("/") {
                resources("")
            }

            get("/astros.json") {
                val result = peopleInSpaceApi.fetchPeople()
                call.respond(result)
            }

            get("/astros_local.json") {
                val result = AstroResult("success", 3,
                    listOf(Assignment("ISS", "Chris Cassidy"),
                        Assignment("ISS", "Anatoly Ivanishin"),
                        Assignment("ISS", "Ivan Vagner")))
                call.respond(result)
            }

        }
    }.start(wait = true)
}
