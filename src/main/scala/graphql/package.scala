/**
 * Created by niuzhaojie on 23/10/16.
 */


package object graphql {

  trait Command extends Serializable {
    def id: String
  }
}
