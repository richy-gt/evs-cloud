import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { environment } from '../../../environments/environment';

interface ApiEndpointOption {
  id: string;
  name: string;
  method: 'GET' | 'POST';
  path: string;
  requiredRole?: string;
  description: string;
  sampleBody?: Record<string, unknown>;
}

@Component({
  selector: 'app-api-tester',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1>Consola de Pruebas API Gateway & BFF</h1>
          <p>Pruebas HTTP reales contra la API configurada</p>
        </div>
      </div>

      <!-- Selector de Endpoint a Probar -->
      <div class="card selector-card">
        <div class="card-header">
          <h2 class="card-title">Seleccionar Endpoint del Microservicio</h2>
          <span class="badge badge-preparacion">Spring Boot en AWS EC2</span>
        </div>

        <div class="endpoints-grid">
          <div
            class="endpoint-item"
            *ngFor="let ep of availableEndpoints"
            [class.selected]="selectedEndpoint.id === ep.id"
            (click)="selectEndpoint(ep)"
          >
            <div class="endpoint-top">
              <span class="http-method" [ngClass]="ep.method.toLowerCase()">{{ ep.method }}</span>
              <code class="ep-path">{{ ep.path }}</code>
            </div>
            <p class="ep-desc">{{ ep.description }}</p>
            <div class="ep-role-tag" *ngIf="ep.requiredRole">
              <span>Requiere Rol:</span>
              <span class="badge badge-alta">{{ ep.requiredRole }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Panel de Ejecucion y Diagnostico -->
      <div class="test-layout">
        <!-- Columna Izquierda: Solicitud HTTP -->
        <div class="card request-card">
          <div class="card-header">
            <h3 class="card-title">1. Solicitud HTTP Saliente</h3>
            <button (click)="executeRequest()" [disabled]="isLoading" class="btn btn-primary btn-sm">
              <svg class="icon-svg" viewBox="0 0 24 24">
                <polygon points="5 3 19 12 5 21 5 3"></polygon>
              </svg>
              <span>{{ isLoading ? 'Enviando...' : 'Ejecutar Peticion' }}</span>
            </button>
          </div>

          <div class="request-details">
            <div class="meta-row">
              <span class="meta-label">Metodo y URL:</span>
              <code class="meta-code">{{ selectedEndpoint.method }} {{ currentFullUrl }}</code>
            </div>

            <div class="headers-section">
              <span class="meta-label">Vista previa de cabeceras (confirma el envio en Network; el token se oculta):</span>
              <pre class="code-block headers-block">{{ requestHeaderPreview | json }}</pre>
            </div>

            <div class="body-section" *ngIf="selectedEndpoint.sampleBody">
              <span class="meta-label">Cuerpo de la Peticion (JSON Payload):</span>
              <pre class="code-block body-block">{{ selectedEndpoint.sampleBody | json }}</pre>
            </div>
          </div>
        </div>

        <!-- Columna Derecha: Respuesta del BFF / Gateway -->
        <div class="card response-card">
          <div class="card-header">
            <h3 class="card-title">2. Respuesta del API Gateway / BFF</h3>
            <span
              class="status-code-badge"
              *ngIf="responseStatus !== null"
              [ngClass]="getStatusBadgeClass()"
            >
              HTTP {{ responseStatus }} {{ responseStatusText }}
            </span>
          </div>

          <div class="response-body-area">
            <div *ngIf="!hasExecuted" class="pending-state">
              Haz clic en "Ejecutar Peticion" para enviar la llamada HTTP a traves de Angular HttpClient y comprobar la validacion del token.
            </div>

            <div *ngIf="hasExecuted">
              <div class="response-json-section">
                <span class="meta-label">Respuesta del Servidor:</span>
                <pre class="code-block response-block">{{ responseData | json }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 24px;
    }

    .selector-card {
      margin-bottom: 24px;
      padding: 24px;
    }

    .endpoints-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 14px;
    }

    .endpoint-item {
      background-color: rgba(0, 0, 0, 0.25);
      border: 1px solid var(--border-subtle);
      border-radius: var(--radius-md);
      padding: 16px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .endpoint-item:hover {
      border-color: rgba(255, 255, 255, 0.2);
      background-color: rgba(255, 255, 255, 0.02);
    }

    .endpoint-item.selected {
      border-color: var(--color-primary);
      background-color: rgba(37, 99, 235, 0.1);
      box-shadow: 0 0 10px rgba(37, 99, 235, 0.2);
    }

    .endpoint-top {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;
    }

    .http-method {
      padding: 2px 8px;
      border-radius: var(--radius-sm);
      font-size: 0.72rem;
      font-weight: 700;
      font-family: var(--font-mono);
    }

    .http-method.get {
      background-color: rgba(16, 185, 129, 0.2);
      color: #34d399;
    }

    .http-method.post {
      background-color: rgba(59, 130, 246, 0.2);
      color: #60a5fa;
    }

    .ep-path {
      font-family: var(--font-mono);
      font-size: 0.85rem;
      color: #f3f4f6;
    }

    .ep-desc {
      font-size: 0.8rem;
      color: var(--text-secondary);
      margin: 0 0 10px 0;
    }

    .ep-role-tag {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.72rem;
      color: var(--text-muted);
    }

    .test-layout {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }

    @media (max-width: 1024px) {
      .test-layout {
        grid-template-columns: 1fr;
      }
    }

    .request-card, .response-card {
      padding: 24px;
    }

    .meta-row {
      margin-bottom: 14px;
    }

    .meta-label {
      display: block;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.04em;
      margin-bottom: 6px;
    }

    .meta-code {
      font-family: var(--font-mono);
      font-size: 0.85rem;
      color: #60a5fa;
      background-color: rgba(0, 0, 0, 0.3);
      padding: 6px 10px;
      border-radius: var(--radius-sm);
      display: block;
      word-break: break-all;
    }

    .headers-block {
      max-height: 180px;
      margin-bottom: 14px;
      font-size: 0.78rem;
    }

    .body-block {
      max-height: 180px;
      font-size: 0.78rem;
    }

    .status-code-badge {
      padding: 4px 10px;
      border-radius: var(--radius-full);
      font-size: 0.8rem;
      font-weight: 700;
      font-family: var(--font-mono);
    }

    .status-code-badge.s200 { background-color: var(--color-success-bg); color: var(--color-success); }
    .status-code-badge.s400 { background-color: var(--color-warning-bg); color: var(--color-warning); }
    .status-code-badge.s500 { background-color: var(--color-danger-bg); color: var(--color-danger); }

    .pending-state {
      text-align: center;
      padding: 60px 20px;
      color: var(--text-muted);
      font-size: 0.85rem;
      line-height: 1.5;
    }

    .validation-checklist {
      background-color: rgba(0, 0, 0, 0.3);
      padding: 16px;
      margin-bottom: 16px;
      border: 1px solid var(--border-subtle);
    }

    .check-title {
      font-size: 0.8rem;
      font-weight: 600;
      color: #cbd5e1;
      margin-bottom: 10px;
    }

    .check-row {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 0.78rem;
      margin-bottom: 6px;
      color: var(--text-secondary);
    }

    .check-row.pass {
      color: #34d399;
    }

    .check-bullet {
      font-family: var(--font-mono);
      font-weight: 700;
      font-size: 0.72rem;
      padding: 1px 5px;
      border-radius: 3px;
      background-color: rgba(255, 255, 255, 0.1);
    }

    .check-row.pass .check-bullet {
      background-color: rgba(16, 185, 129, 0.2);
      color: #10b981;
    }

    .response-block {
      max-height: 280px;
      font-size: 0.8rem;
    }
  `]
})
export class ApiTesterComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);

  public availableEndpoints: ApiEndpointOption[] = [
    {
      id: 'get-orders',
      name: 'Listar Pedidos',
      method: 'GET',
      path: '/pedidos',
      description: 'Consulta el listado general de pedidos autorizados.',
    },
    {
      id: 'get-order-detail',
      name: 'Detalle de Pedido',
      method: 'GET',
      path: '/pedidos/ped-001',
      description: 'Obtiene la informacion detallada de un pedido por ID.',
    },
    {
      id: 'post-order',
      name: 'Crear Pedido',
      method: 'POST',
      path: '/pedidos',
      description: 'Registra una nueva orden con validacion de esquema.',
      sampleBody: {
        clienteNombre: 'Servicios Mineros Atacama Ltda.',
        clienteEmail: 'contacto@mineraatacama.cl',
        clienteTelefono: '+56 9 1234 5678',
        direccionEnvio: 'Av. Ejemplo 123, Antofagasta',
        ciudad: 'Antofagasta',
        total: 2850000,
        estado: 'PENDIENTE',
        prioridad: 'ALTA',
        canalVenta: 'WEB',
        items: [
          { id: 'item-api-test-1', producto: 'Producto de prueba', sku: 'TEST-001', cantidad: 1, precioUnitario: 2850000, subtotal: 2850000 },
        ],
      },
    },
    {
      id: 'admin-metrics',
      name: 'Metricas Administrativas',
      method: 'GET',
      path: '/admin/metricas',
      requiredRole: 'Pedidos.Admin',
      description: 'Endpoint con autorizacion RBAC restringido a rol Admin.',
    },
  ];

  public selectedEndpoint: ApiEndpointOption = this.availableEndpoints[0];
  public isLoading = false;
  public hasExecuted = false;

  public responseStatus: number | null = null;
  public responseStatusText = '';
  public responseData: unknown = null;

  public requestHeaderPreview: Record<string, string> = {};

  ngOnInit(): void {
    this.refreshHeaders();
  }

  public selectEndpoint(ep: ApiEndpointOption): void {
    this.selectedEndpoint = ep;
    this.hasExecuted = false;
    this.responseStatus = null;
    this.refreshHeaders();
  }

  public get currentFullUrl(): string {
    return `${environment.api.baseUrl}${this.selectedEndpoint.path}`;
  }

  private refreshHeaders(): void {
    const tokenInfo = this.authService.inspectToken();
    this.requestHeaderPreview = {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: tokenInfo.rawToken ? 'Bearer [oculto]' : 'No hay token',
      'X-Client-Id': environment.azure.clientId,
      'X-Requested-With': 'Angular-MSAL-DSY1107',
    };

  }

  public executeRequest(): void {
    this.isLoading = true;
    this.hasExecuted = true;
    this.refreshHeaders();

    const targetUrl = this.currentFullUrl;

    const request$ =
      this.selectedEndpoint.method === 'POST'
        ? this.http.post<unknown>(targetUrl, this.selectedEndpoint.sampleBody, { observe: 'response' })
        : this.http.get<unknown>(targetUrl, { observe: 'response' });

    request$.subscribe({
      next: (response) => {
        this.isLoading = false;
        this.responseStatus = response.status;
        this.responseStatusText = response.statusText;
        this.responseData = response.body;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.responseStatus = err.status || null;
        this.responseStatusText = err.statusText || err.message;
        this.responseData = err.error ?? { message: err.message };
      }
    });
  }

  public getStatusBadgeClass(): string {
    if (!this.responseStatus) return '';
    if (this.responseStatus >= 200 && this.responseStatus < 300) return 's200';
    if (this.responseStatus >= 400 && this.responseStatus < 500) return 's400';
    return 's500';
  }
}
