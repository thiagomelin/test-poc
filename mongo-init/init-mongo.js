// Script de inicialização do MongoDB
// Este script é executado automaticamente quando o container MongoDB é iniciado

// Conecta ao banco de dados
db = db.getSiblingDB('orderdb');

// Cria usuário para a aplicação
db.createUser({
  user: 'orderuser',
  pwd: 'orderpass',
  roles: [
    {
      role: 'readWrite',
      db: 'orderdb'
    }
  ]
});

// Configurações de performance para alta volumetria
db.adminCommand({
  setParameter: 1,
  maxTransactionLockRequestTimeoutMillis: 5000
});

print("MongoDB inicializado com sucesso!");
print("Usuário 'orderuser' criado com permissões de leitura/escrita");