$token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLXBydWViYSIsIm5hbWUiOiJVc3VhcmlvIFBydWViYSIsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwicm9sZXNfc2ltdWxhZG9zIjpbIkFETUlOIiwiT1BFUkFUT1IiXX0.simulated-signature"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

$body = @{
    clienteNombre = "Evaluacion Kafka"
    clienteEmail  = "profesor@duoc.cl"
    canalVenta    = "PORTAL_WEB"
    items         = @(
        @{ producto = "Servidor Cloud"; sku = "SRV-01"; cantidad = 1; precioUnitario = 150000 }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" -Method Post -Headers $headers -Body $body
