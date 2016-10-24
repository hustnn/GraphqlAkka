# GraphqlAkka
Akka implementation data store to support Graphql.

Graphql is proposed by Facebook http://graphql.org/.
This project tries to utilizes the akka to implement a backend data storage system for Graphql.
Each record in GraphqlAkka is an actor in Akka.

I have already attempted to show how to support graphql by utilizing akka actor as the backend data store which is shown in the current codebase.

Test:

After running it, you can input the query like this and get the result:

curl -X POST localhost:8080/graphql \
    -H "Content-Type:application/json" \
    -d '{"query": "query Test($humanId: String!){human(id: $humanId) {name, homePlanet, friends {name}}}", "variables": {"humanId": "1000"}}'

Stay tunned for a complete prototype.
I am implementing it use my leasure time. It is very interesting and challenging to get the highly efficient store. Welcome to join me if you are interested in it.


    

