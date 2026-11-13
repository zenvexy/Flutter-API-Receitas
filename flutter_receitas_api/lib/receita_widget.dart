import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:home_widget/home_widget.dart';

class ReceitaWidget {
  static const _nomeDoWidgetAndroid =
      'com.example.flutter_receitas_api.ReceitaWidgetProvider';
  static const _canal = MethodChannel('receita_widget/pin');

  static bool get disponivel =>
      !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  static Future<bool> fixar(Map<String, dynamic> receita) async {
    if (!disponivel) return false;

    final id = receita['id']?.toString();
    if (id == null || id.isEmpty) return false;

    await _salvarReceita(id, receita);
    return await _canal.invokeMethod<bool>('fixar', id) ?? false;
  }

  static Future<void> atualizar(List<Map<String, dynamic>> receitas) async {
    if (!disponivel) return;

    final widgets = await HomeWidget.getInstalledWidgets();
    final idsDasReceitas = <String>{};

    for (final widget in widgets) {
      final widgetId = widget.androidWidgetId;
      if (widgetId == null) continue;

      final receitaId = await HomeWidget.getWidgetData<String>(
        'widget_receita_$widgetId',
      );
      if (receitaId != null) idsDasReceitas.add(receitaId);
    }

    for (final id in idsDasReceitas) {
      Map<String, dynamic>? receita;
      for (final item in receitas) {
        if (item['id']?.toString() == id) {
          receita = item;
          break;
        }
      }
      await _salvarReceita(id, receita);
    }

    await HomeWidget.updateWidget(qualifiedAndroidName: _nomeDoWidgetAndroid);
  }

  static Future<void> _salvarReceita(
    String id,
    Map<String, dynamic>? receita,
  ) async {
    final nome = receita?['nome']?.toString().trim() ?? '';
    final urlDaFoto = receita?['url_imagem']?.toString().trim() ?? '';
    final chaveDaImagem = 'receita_imagem_$id';

    await HomeWidget.saveWidgetData(
      'receita_nome_$id',
      nome.isEmpty ? 'Receita indisponível' : nome,
    );
    await HomeWidget.saveWidgetData(chaveDaImagem, '');

    if (urlDaFoto.isNotEmpty) {
      try {
        await HomeWidget.saveImage(
          chaveDaImagem,
          ResizeImage(NetworkImage(urlDaFoto), width: 320, height: 240),
        ).timeout(const Duration(seconds: 10));
      } catch (_) {
        // Se a foto falhar, o widget mostra a imagem padrão.
      }
    }
  }
}
