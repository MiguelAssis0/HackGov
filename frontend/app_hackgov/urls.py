from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('login/', views.login_view, name='login'),
    path('logout/', views.logout_view, name='logout'),
    # path('register/', views.register_view, name='register'),
    path('dashboard/', views.dashboard, name='dashboard'),
    path('processos/', views.processos, name='processos'),
    path('contato/', views.contato, name='contato'),
    path('ferramentas/', views.ferramentas, name='ferramentas'),
]