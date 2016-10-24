# GraphqlAkka
Akka implementation data store to support Graphql.

Graphql is proposed by Facebook http://graphql.org/.

This project tries to utilize the akka to implement a efficient, robust and large-scale backend data storage system for Graphql.
The mobile applications from billions of devices can efficiently perform the query using the language provided by Graphql and 
get the answers from GraphqlAkka store as faster as possible (our objective is minimizing this response time).

Based on our akka implementation, each record in GraphqlAkka is an actor in Akka which means that the origin optimizations need to be revisted and re-optimized in this scenario. It is challenging but it is also very interesting.

Techniquies:
Graphql
Akka core,
Akka sharding,
Akka persistent(levelDB or Cassandra),
Akka http.

I have already attempted to show how to support graphql by utilizing akka actor as the backend data store which is shown in the current codebase. Currently, the query is parsed in our akka http layer, then the data request is sent to the corresponding actor and get the responses. It shows that my prososal is possbile and I will continue to finish this interesting project.

Test:

After running it, you can input the query like this and get the result:

curl -X POST localhost:8080/graphql \
    -H "Content-Type:application/json" \
    -d '{"query": "query Test($humanId: String!){human(id: $humanId) {name, homePlanet, friends {name}}}", "variables": {"humanId": "1000"}}'

Stay tunned for a complete prototype.

I am implementing it use my leasure time. It is very interesting and challenging to if we want to get a highly efficient store for Graphql. Welcome to join me if you are interested in it.


    

