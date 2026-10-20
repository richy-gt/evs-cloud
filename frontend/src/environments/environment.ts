// Archivo de configuracion de entorno para Pedidos360
// La persona encargada de Azure debe ingresar sus valores en este archivo.

export const environment = {
  production: false,

  // Configuracion de Azure Active Directory (Entra ID)
  azure: {
    // 1. Application (client) ID registrado en Azure Portal
    clientId: 'ea2fb415-bdb9-4bdd-b5bf-f8a75940669e',

    // 2. Directory (tenant) ID registrado en Azure Portal
    tenantId: '58c26126-72f0-428e-b188-c61a51999d72',

    // 3. Authority URL (o tenantId directamente)
    // Ejemplo: https://login.microsoftonline.com/TU_TENANT_ID_DE_AZURE
    authority: 'https://login.microsoftonline.com/58c26126-72f0-428e-b188-c61a51999d72',

    // 4. URI de redireccion autorizada en Azure Portal (Single-Page Application)
    redirectUri: 'http://localhost:4200',

    // 5. URL de cierre de sesion
    postLogoutRedirectUri: 'http://localhost:4200/login',

    // 6. Scopes solicitados para acceder a la API del backend
    // Ejemplo: ['api://TU_BACKEND_CLIENT_ID/access_as_user', 'User.Read']
    scopes: ['api://1bbf593c-a093-4b35-8b78-c61b86831344/access_as_user', 'User.Read'],
  },

  // Configuracion del API Gateway / BFF (Spring Boot en AWS EC2)
  api: {
    // URL base del API Gateway de AWS o BFF
    baseUrl: 'http://localhost:8080/api',
    // Si esta en desarrollo local con Spring Boot:
    localBaseUrl: 'http://localhost:8080/api',
  },

  // Modo demostracion / simulacion:
  // Si clientId es 'TU_CLIENT_ID_DE_AZURE', la aplicacion opera en modo simulacion con datos de prueba
  // para permitir navegar la plataforma completa antes de que se registren las credenciales reales en Azure.
  enableMockAuthWhenUnconfigured: true,
};
