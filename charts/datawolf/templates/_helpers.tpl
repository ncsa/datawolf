{{/*
Expand the name of the chart.
*/}}
{{- define "datawolf.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "datawolf.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "datawolf.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "datawolf.labels" -}}
helm.sh/chart: {{ include "datawolf.chart" . }}
{{ include "datawolf.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "datawolf.selectorLabels" -}}
app.kubernetes.io/name: {{ include "datawolf.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "datawolf.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "datawolf.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the url to connect to postgresql
*/}}
{{- define "datawolf.postgresql" -}}
{{- if .Values.postgresql.enabled -}}
postgresql://{{ include "datawolf.name" . }}-postgresql-headless/{{ .Values.postgresql.postgresqlDatabase }}
{{- else }}
{{- .Values.postgresql.url }}
{{- end }}
{{- end }}

{{/*
Create the pvc name for datawolf
*/}}
{{- define "datawolf.pvc" -}}
{{- if .Values.persistence.existingClaim -}}
{{ .Values.persistence.existingClaim }}
{{- else -}}
{{ include "datawolf.fullname" . }}
{{- end }}
{{- end }}
