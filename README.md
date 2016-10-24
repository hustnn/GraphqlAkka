# GraphqlAkka
Akka implementation data store to support Graphql

I have already attempted to show how to support graphql by utilizing akka actor as the backend data store.
Stay tunned for a complete prototype.

Test:

After running it, you can input the query like this and get the result:

curl -X POST localhost:8080/graphql \
    -H "Content-Type:application/json" \
    -d '{"query": "query Test($humanId: String!){human(id: $humanId) {name, homePlanet, friends {name}}}", "variables": {"humanId": "1000"}}'
    

